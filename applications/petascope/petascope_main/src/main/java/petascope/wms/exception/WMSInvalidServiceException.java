
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

package petascope.wms.exception;

import petascope.exceptions.WMSException;
import petascope.exceptions.ExceptionCode;

/**
 * Exception to be used when the service requested is invalid
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class WMSInvalidServiceException extends WMSException {
    /**
     * Constructor for the class
     *
     * @param suppliedService the user indicated service
     */
    public WMSInvalidServiceException(String suppliedService) {
        super(ExceptionCode.InvalidRequest, ERROR_MESSAGE.replace("$Service", suppliedService));
    }

    private static final String ERROR_MESSAGE = "The requested service '$Service' is invalid.";
}
