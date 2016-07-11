/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.metadata.service;

import petascope.wcps2.error.managed.processing.CoverageAxisNotFoundExeption;
import petascope.wcps2.error.managed.processing.IncompatibleAxesNumberException;
import petascope.wcps2.metadata.model.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import petascope.core.CrsDefinition;
import petascope.exceptions.PetascopeException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.wcps2.error.managed.processing.InvalidSubsettingException;
import petascope.wcps2.error.managed.processing.OutOfBoundsSubsettingException;
import petascope.wcps2.error.managed.processing.RangeFieldNotFound;

/**
 * Class responsible with offering functionality for doing operations on
 * WcpsCoverageMetadataObjects.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WcpsCoverageMetadataService {

    private final CoordinateTranslationService coordinateTranslationService;

    public WcpsCoverageMetadataService(CoordinateTranslationService coordinateTranslationService) {
        this.coordinateTranslationService = coordinateTranslationService;
    }

    /**
     * Creates a resulting coverage metadata object when a binary operation is
     * performed. The current convention is the following: if only 1 object is
     * non null, the that is the passed meta. If both are non null, the first
     * one is passed. If both are null, null is passed.
     *
     * @param firstMeta
     * @param secondMeta
     * @return
     */
    public WcpsCoverageMetadata getResultingMetadata(WcpsCoverageMetadata firstMeta, WcpsCoverageMetadata secondMeta) {
        checkCompatibility(firstMeta, secondMeta);
        if (firstMeta != null) {
            return firstMeta;
        }
        if (firstMeta == null && secondMeta != null) {
            return secondMeta;
        }
        //default both are null
        return null;
    }

    /**
     * Applies subsetting to a metadata object. e.g: eobstest(t(0:5),
     * Lat(-40.5:75), Long(25.5:75)) and with the trimming expression
     * (c[Lat(20:30), Long(40:50)]) then need to apply the subsets [(20:30),
     * (40:50)] in the coverage metadata expression
     *
     * @param checkBoundary should the subset needed to check the boundary (e.g: with scale(..., {subset})) will not need to check.
     * @param metadata
     * @param subsetList
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public WcpsCoverageMetadata applySubsets(Boolean checkBoundary, WcpsCoverageMetadata metadata, List<Subset> subsetList) throws PetascopeException {
        checkSubsetConsistency(metadata, subsetList);
        // iterate through the subsets
        // Normally, the query will need to calculate the grid bound from geo bound
        boolean calculateGridBound = true;
        for (Subset subset : subsetList) {
            //identify the corresponding axis in the coverage metadata
            for (Axis axis : metadata.getAxes()) {
                // Only apply to correspondent axis with same name
                if (axis.getLabel().equals(subset.getAxisName())) {
                    // If subset has a given CRS, e.g: Lat:"http://../3857" then change the CRS in axis as well
                    if (subset.getCrs() != null && !subset.getCrs().equals(axis.getCrsUri())) {
                        axis.setCrsUri(subset.getCrs());
                    }
                    if (axis.getCrsUri().equals(CrsUtil.GRID_CRS)) {
                        // it will need to calculate from grid bound to geo bound (e.g: Lat:"http://.../Index2D"(0:50) -> Lat(0:20))
                        calculateGridBound = false;
                    }

                    // NOTE: There are 2 types of subset:
                    // + update the geo-bound according to the subsets and translate updated geo-bound to new grid-bound
                    //   e.g: Lat(0:20) -> c[0:50] (calculate the grid coordinates from geo coordinates)
                    // + update the grid-bound according to the subsets and translate update grid-bound to new geo-bound
                    //   e.g: Lat:"http://..../Index2D"(0:50) -> Lat(0:20) (calculate the geo coordinates from grid coordinates)
                    // Trimming
                    if (subset.getNumericSubset() instanceof NumericTrimming) {
                        applyTrimmingSubset(calculateGridBound, checkBoundary, metadata, subset, axis);
                    } else {
                        // slicing
                        applySlicing(calculateGridBound, checkBoundary, metadata, subset, axis);
                    }

                    // Continue with another subset
                    break;
                }
            }
        }

        return metadata;
    }

    /**
     * Strip slicing axes from coverage's metadata then they are not included
     * when re (slice/trim) the coverage. e.g: slice ( slice(c[t(0:5),
     * Lat(25:70), Long(30:60)], {t(0)}), {Lat(30)} ) the output is 1D in the
     * Long axis
     *
     * @param metadata
     */
    public void stripSlicingAxes(WcpsCoverageMetadata metadata) {
        List<Integer> removedIndexs = new ArrayList<Integer>();
        int i = 0;
        for (Axis axis : metadata.getAxes()) {
            if (axis.getGeoBounds() instanceof NumericSlicing) {
                removedIndexs.add(i);
            }
            i++;
        }

        // Remove the slicing axes from the coverage
        int removedIndex = 0;
        for (int index : removedIndexs) {
            metadata.getAxes().remove(index - removedIndex);
            removedIndex++;
        }
    }

    /**
     * Get the index of field name in the coverage
     *
     * @param metadata
     * @param fieldName
     * @return
     */
    public int getRangeFieldIndex(WcpsCoverageMetadata metadata, String fieldName) {
        int index = 0;
        for (RangeField rangeField : metadata.getRangeFields()) {
            if (rangeField.getName().equals(fieldName)) {
                // e.g: c.red
                return index;
            } else if (fieldName.equals(String.valueOf(index))) {
                // e.g: c.1
                return index;
            }
            index++;
        }
        // Cannot found range field in coverage then throws error
        throw new RangeFieldNotFound(fieldName);
    }

    /**
     * Checks if the range field is present in the coverage.
     *
     * @param metadata
     * @param fieldName
     * @return
     */
    public boolean checkIfRangeFieldExists(WcpsCoverageMetadata metadata, String fieldName) {
        boolean found = false;
        for (RangeField rangeField : metadata.getRangeFields()) {
            if (rangeField.getName().equals(fieldName)) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Checks if the selected range is at a valid index for the current
     * coverage.
     *
     * @param metadata
     * @param rangeField
     * @return
     */
    public boolean checkRangeFieldNumber(WcpsCoverageMetadata metadata, int rangeField) {
        if (rangeField > metadata.getRangeFields().size() - 1 || rangeField < 0) {
            return false;
        }
        return true;
    }

    /**
     * Creates a coverage for the coverage constructor. Right now, this is not
     * geo-referenced.
     *
     * @param coverageName
     * @param numericSubsets
     * @return
     */
    public WcpsCoverageMetadata createCoverage(String coverageName, List<Subset> numericSubsets) {
        //create a new axis for each subset
        List<Axis> axes = new ArrayList();
        int axesCounter = 0;
        for (Subset numericSubset : numericSubsets) {
            String label = numericSubset.getAxisName();

            NumericSubset geoBounds = null;
            NumericSubset gridBounds = null;

            if (numericSubset.getNumericSubset() instanceof NumericTrimming) {
                BigDecimal lowerLimit = ((NumericTrimming) numericSubset.getNumericSubset()).getLowerLimit();
                BigDecimal upperLimit = ((NumericTrimming) numericSubset.getNumericSubset()).getUpperLimit();

                // trimming
                geoBounds = new NumericTrimming(lowerLimit, upperLimit);
                //for now, the geoDomain is the same as the gridDomain, as we do no conversion in the coverage constructor / condenser
                gridBounds = new NumericTrimming(lowerLimit, upperLimit);
            } else {
                BigDecimal bound = ((NumericSlicing) numericSubset.getNumericSubset()).getBound();

                // slicing
                geoBounds = new NumericSlicing(bound);
                gridBounds = new NumericSlicing(bound);
            }

            // the crs of axis
            String crsUri = numericSubset.getCrs();

            //the axis direction should be deduced from the crs, when we'll support geo referencing in the coverage constructor
            //probably with a service. crsService.getAxisDirection(crs, axisLabel)
            AxisDirection axisDirection = CrsDefinition.getAxisDirection(label);

            // the created coverage now is only RectifiedGrid then it will use GridSpacing UoM
            String axisUoM = CrsUtil.INDEX_UOM;

            // Create a crsDefintion by crsUri
            CrsDefinition crsDefinition = null;
            if(crsUri != null && ! crsUri.equals("")) {
                CrsUtility.getCrsDefinitionByCrsUri(crsUri);
            }

            // the axis type (x, y, t,...) should be set to axis correctly, now just set to x
            String axisType = AxisTypes.X_AXIS;

            // Scalar resolution is set to 1
            BigDecimal scalarResolution = CrsUtil.INDEX_SCALAR_RESOLUTION;

            Axis axis = new Axis(label, geoBounds, gridBounds, axisDirection, crsUri, crsDefinition, axisType, axisUoM, scalarResolution, axesCounter);
            axesCounter++;
            axes.add(axis);
        }
        //the current crs if GRID CRS. When the coverage constructor will support geo referencing, the CrsService should
        //deduce the crs from the crses of the axes
        // NOTE: now, just use gridCrsUri (e.g: http://.../IndexND) to set as crs for creating coverage first
        String gridCrsUri = CrsUtility.getImageCrsUri(axes);
        WcpsCoverageMetadata result = new WcpsCoverageMetadata(coverageName, axes, gridCrsUri, gridCrsUri, null);
        return result;
    }

    /**
     * Apply trimming subset to regular/irregular axis and change geo bounds and
     * grid bounds of coverage metadata e.g: subset: Lat(0:20) with coverage
     * (Lat(0:70)) then need to update coverage metadata with geo bound(0:20)
     * and correspondent translated grid bound from the new geo bound.
     *
     * @param calculateGeoBound
     * @param checkBoundary
     * @param metadata
     * @param subset
     * @param axis
     * @throws PetascopeException
     */
    private void applyTrimmingSubset(Boolean calculateGridBound, Boolean checkBoundary, WcpsCoverageMetadata metadata, Subset subset, Axis axis) throws PetascopeException {
        //set the lower, upper bounds and crs
        BigDecimal lowerLimit = ((NumericTrimming) subset.getNumericSubset()).getLowerLimit();
        BigDecimal upperLimit = ((NumericTrimming) subset.getNumericSubset()).getUpperLimit();

        ParsedSubset<BigDecimal> parsedSubset = new ParsedSubset<BigDecimal>(lowerLimit, upperLimit);

        // Check if trim (lo > high)
        if (lowerLimit.compareTo(upperLimit) > 0) {
            throw new InvalidSubsettingException(axis.getLabel(),
                    new ParsedSubset<String>(lowerLimit.toPlainString(), upperLimit.toPlainString()));
        }

        if (checkBoundary) {
            // NOTE: if crs is not Index%d then need to check boundary with the geo else check with grid (e.g: Lat:"http://.../Index2D"(0:20))
            if (calculateGridBound) {
                validParsedSubsetGeoBounds(parsedSubset, axis);
            } else {
                validParsedSubsetGridBounds(parsedSubset, axis);
            }
        }

        // Translate geo subset -> grid subset or grid subset -> geo subset
        NumericTrimming unAppliedNumericSubset = null;
        NumericTrimming unTranslatedNumericSubset = null;
        if (calculateGridBound) {
            // Lat(0:20) -> c[0:50]
            unAppliedNumericSubset = (NumericTrimming)axis.getGeoBounds();
            unTranslatedNumericSubset = (NumericTrimming)axis.getGridBounds();
            this.translateTrimmingSubset(calculateGridBound, axis, subset, unAppliedNumericSubset, unTranslatedNumericSubset, metadata);
        } else {
            // c[0:50] -> Lat(0:20)
            unAppliedNumericSubset = (NumericTrimming)axis.getGridBounds();
            unTranslatedNumericSubset = (NumericTrimming)axis.getGeoBounds();
            this.translateTrimmingSubset(calculateGridBound, axis, subset, unAppliedNumericSubset, unTranslatedNumericSubset, metadata);
        }
    }

    /**
     * Apply the trimming subset on the unAppliedNumericSubset (geo/grid bounds) and
     * calculate this bound to unTranslatedNumericSubset (grid/geo bound)
     * @param calculateGridBound
     * @param axis
     * @param subset
     * @param unAppliedNumericSubset
     * @param unTranslatedNumericSubset
     * @param metadata
     * @throws PetascopeException
     */
    private void translateTrimmingSubset(boolean calculateGridBound, Axis axis, Subset subset,
                        NumericTrimming unAppliedNumericSubset, NumericTrimming unTranslatedNumericSubset,
                        WcpsCoverageMetadata metadata) throws PetascopeException {
        BigDecimal geoDomainMin = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit();
        BigDecimal geoDomainMax = ((NumericTrimming) axis.getGeoBounds()).getUpperLimit();
        BigDecimal gridDomainMin = ((NumericTrimming) axis.getGridBounds()).getLowerLimit();
        BigDecimal gridDomainMax = ((NumericTrimming) axis.getGridBounds()).getUpperLimit();

        BigDecimal lowerLimit = ((NumericTrimming) subset.getNumericSubset()).getLowerLimit();
        BigDecimal upperLimit = ((NumericTrimming) subset.getNumericSubset()).getUpperLimit();

        // Apply the subset on the unAppliedNumericSubset
        unAppliedNumericSubset.setLowerLimit(lowerLimit);
        unAppliedNumericSubset.setUpperLimit(upperLimit);

        ParsedSubset<BigDecimal> parsedSubset = new ParsedSubset<BigDecimal>(lowerLimit, upperLimit);
        // store the translated grid bounds from the subsets
        ParsedSubset<BigInteger> translatedSubset;


        // Regular axis (no need to query database)
        if (axis instanceof RegularAxis) {
            BigDecimal resolution = ((RegularAxis) axis).getResolution();
            if (calculateGridBound) {
                // Lat(0:20) -> c[0:50]
                translatedSubset = coordinateTranslationService.getNumericPixelIndicesForRegularAxis(calculateGridBound,
                                   parsedSubset, geoDomainMin, geoDomainMax, resolution, gridDomainMin);
            } else {
                // Lat:"http://.../Index2D"(0:50) -> Lat(0:20)
                translatedSubset = coordinateTranslationService.getNumericPixelIndicesForRegularAxis(calculateGridBound,
                                   parsedSubset, gridDomainMin, gridDomainMax, resolution, geoDomainMin);
            }
        } else {
            // Irregular axis (query database for coefficients)
            int iOrder = ((IrregularAxis) axis).getiOrder();
            BigDecimal scalarResolution = axis.getScalarResolution();
            if (calculateGridBound) {
                // e.g: ansi(148654) in irr_cube_2 -> c[0]
                translatedSubset = coordinateTranslationService.getNumericPixelIndicesForIrregularAxes(
                        parsedSubset, scalarResolution, metadata.getCoverageName(), iOrder, gridDomainMin, gridDomainMax, geoDomainMin);
            } else {
                // NOTE: if subsettingCrs is IndexCrs, ( e.g: ansi:"http://.../Index3D"(3) ) in irr_cube_2
                // it is query directly in grid coordinate which is regular not irregular anymore
                // Problem: it cannot resolve from grid coordinate (e.g: c[3]) to "geo" coordinate (e.g: 148661) with irregular axis.
                // then consider geo bound is equal to grid bound in this case.
                translatedSubset = new ParsedSubset<BigInteger>(lowerLimit.toBigInteger(), upperLimit.toBigInteger());
            }
        }

        // Set the correct translated grid parsed subset to axis
        unTranslatedNumericSubset.setLowerLimit(new BigDecimal(translatedSubset.getLowerLimit()));
        unTranslatedNumericSubset.setUpperLimit(new BigDecimal(translatedSubset.getUpperLimit()));
    }


    /**
     * Apply slicing subset to regular/irregular axis and change geo bounds and
     * grid bounds of coverage metadata e.g: subset: Lat(20) with coverage
     * (Lat(0:70)) then need to update coverage metadata with geo bound(20) and
     * correspondent translated grid bound from the new geo bound.
     * @param calculateGridBound
     * @param checkBoundary should subset needed to be check within boundary (e.g: scale(..., {subset}) does not need to check)
     * @param metadata
     * @param subset
     * @param axis
     * @throws PetascopeException
     */
    private void applySlicing(Boolean calculateGridBound, Boolean checkBoundary, WcpsCoverageMetadata metadata, Subset subset, Axis axis) throws PetascopeException {
        BigDecimal bound = ((NumericSlicing) subset.getNumericSubset()).getBound();
        ParsedSubset<BigDecimal> geoParsedSubset = new ParsedSubset<BigDecimal>(bound, bound);
        // check if parsed subset is valid
        if (checkBoundary) {
            // NOTE: if crs is not Index%d then need to check boundary with the geo else check with grid (e.g: Lat:"http://.../Index2D"(0:20))
            if (!axis.getCrsUri().equals(CrsUtil.GRID_CRS)) {
                validParsedSubsetGeoBounds(geoParsedSubset, axis);
            } else {
                validParsedSubsetGridBounds(geoParsedSubset, axis);
            }
        }

        // Translate geo subset -> grid subset or grid subset -> geo subset
        this.translateSlicingSubset(calculateGridBound, axis, subset, metadata);

    }

    /**
     * Apply the slicing subset on the (geo/grid bounds) and
     * calculate this bound to (grid/geo bound)
     * @param calculateGridBound
     * @param axis
     * @param subset
     * @param metadata
     * @throws PetascopeException
     */
    private void translateSlicingSubset(boolean calculateGridBound, Axis axis, Subset subset,
                                        WcpsCoverageMetadata metadata) throws PetascopeException {

        BigDecimal bound = ((NumericSlicing) subset.getNumericSubset()).getBound();
        ParsedSubset<BigDecimal> parsedSubset = new ParsedSubset<BigDecimal>(bound, bound);
        // store the translated grid bounds from the subsets
        ParsedSubset<BigInteger> translatedSubset;

        // Translate the coordinate in georeferenced to grid.
        BigDecimal geoDomainMin;
        BigDecimal geoDomainMax;
        BigDecimal gridDomainMin;
        BigDecimal gridDomainMax;

        // NOTE: before applying slicing subset on axis, it can be trimming ( e.g: slice(c[Lat(0:20)]), {Lat(5)}) )
        if (axis.getGridBounds() instanceof NumericSlicing) {
            // slicing axis
            geoDomainMin = ((NumericSlicing) axis.getGeoBounds()).getBound();
            geoDomainMax = geoDomainMin;
            gridDomainMin = ((NumericSlicing) axis.getGridBounds()).getBound();
            gridDomainMax = gridDomainMin;
        } else {
            // trimming axis
            geoDomainMin = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit();
            geoDomainMax = ((NumericTrimming) axis.getGeoBounds()).getUpperLimit();
            gridDomainMin = ((NumericTrimming) axis.getGridBounds()).getLowerLimit();
            gridDomainMax = ((NumericTrimming) axis.getGridBounds()).getUpperLimit();
        }


        // NOTE: numeric type of axis here can be trimming when building axes for the coverage, it need to be change to slicing
        NumericSlicing numericSlicingBound = new NumericSlicing(bound);
        if (calculateGridBound) {
            // Lat(20) -> c(50)
            axis.setGeoBounds(numericSlicingBound);
        } else {
            // Lat:"http://../Index2D"(50) -> Lat(20)
            axis.setGridBounds(numericSlicingBound);
        }

        // Regular Axis
        if (axis instanceof RegularAxis) {
            BigDecimal resolution = ((RegularAxis) axis).getResolution();
            if (calculateGridBound) {
                // Lat(0:20) -> c[0:50]
                translatedSubset = coordinateTranslationService.getNumericPixelIndicesForRegularAxis(calculateGridBound,
                                   parsedSubset, geoDomainMin, geoDomainMax, resolution, gridDomainMin);
            } else {
                // Lat:"http://.../Index2D"(0:50) -> Lat(0:20)
                translatedSubset = coordinateTranslationService.getNumericPixelIndicesForRegularAxis(calculateGridBound,
                                   parsedSubset, gridDomainMin, gridDomainMax, resolution, geoDomainMin);
            }
        } else {
            // Irregular Axis
            int iOrder = ((IrregularAxis) axis).getiOrder();
            BigDecimal scalarResolution = axis.getScalarResolution();
            if (calculateGridBound) {
                // e.g: ansi(148654) in irr_cube_2 -> c[0]
                translatedSubset = coordinateTranslationService.getNumericPixelIndicesForIrregularAxes(
                                   parsedSubset, scalarResolution, metadata.getCoverageName(), iOrder,
                                   gridDomainMin, gridDomainMax, geoDomainMin);
            } else {
                // NOTE: if subsettingCrs is IndexCrs, ( e.g: ansi:"http://.../Index3D"(3) ) in irr_cube_2
                // it is query directly in grid coordinate which is regular not irregular anymore
                // Problem: it cannot resolve from grid coordinate (e.g: c[3]) to "geo" coordinate (e.g: 148661) with irregular axis.
                // then consider geo bound is equal to grid bound in this case.
                translatedSubset = new ParsedSubset<BigInteger>(bound.toBigInteger(), bound.toBigInteger());
            }
        }

        // Set the correct translated grid parsed subset to axis
        numericSlicingBound = new NumericSlicing(new BigDecimal(translatedSubset.getLowerLimit()));
        if (calculateGridBound) {
            axis.setGridBounds(numericSlicingBound);
        } else {
            axis.setGeoBounds(numericSlicingBound);
        }
    }

    /**
     * Check if parsed subset is inside the geo domain of the current of the axis
     * e.g: axis's geo domain: Lat(0:50), and parsed subset: Lat(15:70) is out of upper bound
     * @param geoParsedSubset
     * @param axis
     */
    private void validParsedSubsetGeoBounds(ParsedSubset<BigDecimal> geoParsedSubset, Axis axis) {
        String axisName = axis.getLabel();

        // Check if subset is valid with trimming geo bound
        if (axis.getGeoBounds() instanceof NumericTrimming) {
            BigDecimal lowerLimit = ((NumericTrimming)axis.getGeoBounds()).getLowerLimit();
            BigDecimal upperLimit = ((NumericTrimming)axis.getGeoBounds()).getUpperLimit();
            ParsedSubset<String> subset;

            // Check if subset is inside the domain of geo bound
            if (geoParsedSubset.isSlicing()) {
                // slicing geo parsed subset
                if (  ( geoParsedSubset.getSlicingCoordinate().compareTo(lowerLimit) < 0 )
                   || ( geoParsedSubset.getSlicingCoordinate().compareTo(upperLimit) > 0 ) ) {

                    // throw slicing error
                    subset = new ParsedSubset<String>(geoParsedSubset.getSlicingCoordinate().toPlainString());
                    throw new OutOfBoundsSubsettingException(axisName, subset, lowerLimit.toPlainString(), upperLimit.toPlainString());
                }
            } else {
                // trimming geo parsed subset
                if (  ( geoParsedSubset.getLowerLimit().compareTo(lowerLimit) < 0 )
                   || ( geoParsedSubset.getLowerLimit().compareTo(upperLimit) > 0 )
                   || ( geoParsedSubset.getUpperLimit().compareTo(lowerLimit) < 0 )
                   || ( geoParsedSubset.getUpperLimit().compareTo(upperLimit) > 0 ) ) {

                    // throw trimming error
                    subset = new ParsedSubset<String>(geoParsedSubset.getLowerLimit().toPlainString(),
                                                      geoParsedSubset.getUpperLimit().toPlainString());
                    throw new OutOfBoundsSubsettingException(axisName, subset, lowerLimit.toPlainString(), upperLimit.toPlainString());
                }
            }
        } else {
            // Check if subset is valid with slicing geo bound
            BigDecimal bound = ((NumericSlicing)axis.getGeoBounds()).getBound();
            ParsedSubset<String> subset = new ParsedSubset<String>(geoParsedSubset.getLowerLimit().toPlainString());

            // Cannot pass a trimming subset in a slicing subset
            if (geoParsedSubset.isTrimming()) {
                throw new InvalidSubsettingException(axisName, subset);
            }

            // Check if subset is equal with slicing bound of geo bound
            if (!geoParsedSubset.getLowerLimit().equals(bound)) {
                throw new OutOfBoundsSubsettingException(axisName, subset,
                                                        bound.toPlainString(), bound.toPlainString());
            }
        }
    }


    /**
     * Check if parsed subset is inside the grid domain of the current of the axis
     * e.g: axis's grid domain: Lat(0:50), and parsed subset: Lat:"http://.../Index2D"(65:80) is out of upper bound
     * @param gridParsedSubset
     * @param axis
     */
    private void validParsedSubsetGridBounds(ParsedSubset<BigDecimal> gridParsedSubset, Axis axis) {
        String axisName = axis.getLabel();

        // Check if subset is valid with trimming grid bound
        if (axis.getGridBounds() instanceof NumericTrimming) {
            BigDecimal lowerLimit = ((NumericTrimming)axis.getGridBounds()).getLowerLimit();
            BigDecimal upperLimit = ((NumericTrimming)axis.getGridBounds()).getUpperLimit();
            ParsedSubset<String> subset;

            // Check if subset is inside the domain of grid bound
            if (gridParsedSubset.isSlicing()) {
                // slicing grid parsed subset
                if (  ( gridParsedSubset.getSlicingCoordinate().compareTo(lowerLimit) < 0 )
                   || ( gridParsedSubset.getSlicingCoordinate().compareTo(upperLimit) > 0 ) ) {

                    // throw slicing error
                    subset = new ParsedSubset<String>(gridParsedSubset.getSlicingCoordinate().toPlainString());
                    throw new OutOfBoundsSubsettingException(axisName, subset, lowerLimit.toPlainString(), upperLimit.toPlainString());
                }
            } else {
                // trimming grid parsed subset
                if (  ( gridParsedSubset.getLowerLimit().compareTo(lowerLimit) < 0 )
                   || ( gridParsedSubset.getLowerLimit().compareTo(upperLimit) > 0 )
                   || ( gridParsedSubset.getUpperLimit().compareTo(lowerLimit) < 0 )
                   || ( gridParsedSubset.getUpperLimit().compareTo(upperLimit) > 0 ) ) {

                    // throw trimming error
                    subset = new ParsedSubset<String>(gridParsedSubset.getLowerLimit().toPlainString(),
                                                      gridParsedSubset.getUpperLimit().toPlainString());
                    throw new OutOfBoundsSubsettingException(axisName, subset, lowerLimit.toPlainString(), upperLimit.toPlainString());
                }
            }
        } else {
            // Check if subset is valid with slicing grid bound
            BigDecimal bound = ((NumericSlicing)axis.getGridBounds()).getBound();
            ParsedSubset<String> subset = new ParsedSubset<String>(gridParsedSubset.getLowerLimit().toPlainString());

            // Cannot pass a trimming subset in a slicing subset
            if (gridParsedSubset.isTrimming()) {
                throw new InvalidSubsettingException(axisName, subset);
            }

            // Check if subset is equal with slicing bound of grid bound
            if (!gridParsedSubset.getLowerLimit().equals(bound)) {
                throw new OutOfBoundsSubsettingException(axisName, subset,
                                                        bound.toPlainString(), bound.toPlainString());
            }
        }
    }

    private void checkSubsetConsistency(WcpsCoverageMetadata metadata, List<Subset> subsetList) {
        //check if all the subset axis exist in the coverage
        for (Subset dimension : subsetList) {
            if (!checkAxisExists(dimension.getAxisName(), metadata)) {
                throw new CoverageAxisNotFoundExeption(dimension.getAxisName());
            }
        }
        //check if the subset is withing the bounds of the axis is not done. @TODO: check if this is needed
    }

    private boolean checkAxisExists(String axisName, WcpsCoverageMetadata metadata) {
        boolean found = false;
        for (Axis axis : metadata.getAxes()) {
            if (axis.getLabel().equals(axisName)) {
                found = true;
                break;
            }
        }
        return found;
    }

    private void checkCompatibility(WcpsCoverageMetadata firstMeta, WcpsCoverageMetadata secondMeta) {
        //we want to detect only the cases where an error should be thrown
        if (firstMeta != null && secondMeta != null) {
            //check number of axes to be the same
            if (firstMeta.getAxes().size() != secondMeta.getAxes().size()) {
                throw new IncompatibleAxesNumberException(firstMeta.getCoverageName(), secondMeta.getCoverageName(),
                        firstMeta.getAxes().size(), secondMeta.getAxes().size());
            }
            //we don't check right now if the axes labels are different. If needed, add here.
        }
    }

}
