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
package petascope.wcps.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;

/**
 * Error message for not identical axes are used in crsTransform(..., {Lat:"CRS_A", Long:"CRS_B"}, { });
 *
 * @author <a href="mailto:bphamhuu@jacbos-university.de">Bang Pham Huu</a>
 */
public class NotIdenticalCrsInCrsTransformException extends WCPSException {
    /**
     * Constructor for the class
     *
     * @param crsX outputCrs in axis X
     * @param crsY outputCrs in axis Y
     */
    public NotIdenticalCrsInCrsTransformException(String crsX, String crsY) {
        super(ExceptionCode.InvalidRequest, ERROR_TEMPLATE.replace("$CRS_X", crsX)
                            .replace("$CRS_Y", crsY));
    }

    private static final String ERROR_TEMPLATE = "Not identical CRS are used in crsTransform, received '$CRS_X' and '$CRS_Y'.";
}