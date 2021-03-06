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

import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WMSException;

/**
 * Exception to be thrown when a layer already existed in database but another
 * InsertWCSLayer is sent with same layer name.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class WMSDuplicateLayerException extends WMSException {

    /**
     * Constructor for the class
     *
     * @param layerName the requested layer by the client
     */
    public WMSDuplicateLayerException(String layerName) {
        super(ExceptionCode.InvalidRequest, ERROR_MESSAGE.replace("$LayerName", layerName));
    }

    private final static String ERROR_MESSAGE = "The requested layer '$LayerName' already existed in database.";
}
