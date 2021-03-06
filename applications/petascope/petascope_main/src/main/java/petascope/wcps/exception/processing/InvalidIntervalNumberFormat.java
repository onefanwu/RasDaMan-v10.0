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
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class InvalidIntervalNumberFormat extends WCPSException {
    
    public InvalidIntervalNumberFormat(String low, String high, String errorMessage) {
        super(ExceptionCode.InvalidRequest, EXCEPTION_TEXT.replace("$low", low)
              .replace("$high", high)
              .replace("$errorMessage", errorMessage));
    }
    
    public InvalidIntervalNumberFormat(String low, String high, String errorMessage, Exception cause) {
        super(ExceptionCode.InvalidRequest, EXCEPTION_TEXT.replace("$low", low)
              .replace("$high", high)
              .replace("$errorMessage", errorMessage), cause);
    }

    private static final String EXCEPTION_TEXT = "Invalid number format in interval ($low:$high). Reason: $errorMessage";
}
