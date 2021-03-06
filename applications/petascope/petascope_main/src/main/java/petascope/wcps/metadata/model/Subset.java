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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.metadata.model;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class Subset {

    private NumericSubset numericSubset;
    private String crs;
    private final String axisName;

    public Subset(NumericSubset numericSubset, String crs, String axisName) {
        this.numericSubset = numericSubset;
        this.crs = crs;
        this.axisName = axisName;
    }
    
    public void setNumericSubset(NumericSubset numericSubset) {
        this.numericSubset = numericSubset;
    }

    public NumericSubset getNumericSubset() {
        return numericSubset;
    }
    
    public void setCrs(String crs) {
        this.crs = crs;
    }

    public String getCrs() {
        return crs;
    }

    public String getAxisName() {
        return axisName;
    }
}
