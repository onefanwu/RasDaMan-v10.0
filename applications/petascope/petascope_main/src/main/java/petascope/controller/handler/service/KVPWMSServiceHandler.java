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
package petascope.controller.handler.service;

import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.AbstractController;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import petascope.exceptions.WMSException;
import petascope.wms.handlers.kvp.KVPWMSGetCapabilitiesHandler;
import petascope.wms.handlers.kvp.KVPWMSGetLegendGraphicHandler;
import petascope.wms.handlers.kvp.KVPWMSGetMapHandler;

/**
 * Main handler for all WMS requests.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWMSServiceHandler extends AbstractHandler {

    @Autowired
    private KVPWMSGetCapabilitiesHandler getCapabilitiesHandler;
    @Autowired
    private KVPWMSGetMapHandler getMapHandler;
    @Autowired
    private KVPWMSGetLegendGraphicHandler getLegendGraphicHandler;

    public KVPWMSServiceHandler() {
        // WMS is a part of WMS 1.3
        service = KVPSymbols.WMS_SERVICE;

        requestServices.add(KVPSymbols.VALUE_GET_CAPABILITIES);
        requestServices.add(KVPSymbols.VALUE_WMS_GET_MAP);
        requestServices.add(KVPSymbols.VALUE_WMS_GET_LEGEND_GRAPHIC);
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws WCSException, IOException, PetascopeException, SecoreException, WMSException, Exception {
        String requestService = AbstractController.getValueByKey(kvpParameters, KVPSymbols.KEY_REQUEST);
        Response response = null;

        // GetCapabilities
        if (requestService.equals(KVPSymbols.VALUE_GET_CAPABILITIES)) {
            response = getCapabilitiesHandler.handle(kvpParameters);
        } else if (requestService.equals(KVPSymbols.VALUE_WMS_GET_MAP)) {
            // GetMap
            response = getMapHandler.handle(kvpParameters);
        } else if (requestService.equals(KVPSymbols.VALUE_WMS_GET_LEGEND_GRAPHIC)) {
            // GetLegendGraphic
            response = getLegendGraphicHandler.handle(kvpParameters);
        }

        return response;
    }
}
