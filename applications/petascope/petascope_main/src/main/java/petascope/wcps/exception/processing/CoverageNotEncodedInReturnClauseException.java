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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;

/**
 * Exception that is thrown when a non-scalar coverage result is not encoded.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-univeristy.de">Bang Pham Huu</a>
 */
public class CoverageNotEncodedInReturnClauseException extends WCPSException {

    public CoverageNotEncodedInReturnClauseException() {
        super(ExceptionCode.InvalidRequest, ERROR_TEMPLATE);
    }

    private static final String ERROR_TEMPLATE = "Coverage result must use encode() as it is non-scalar value.";
}
