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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcst.exceptions;
import petascope.exceptions.WCSTException;
import petascope.exceptions.ExceptionCode;
/**
 * Throw an exception when a parent XML element does not contain only one child element
 * 
 @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class WCSTRequiredOneElement extends WCSTException {
    
    public WCSTRequiredOneElement(String parentElement, String childElement) {
        super(ExceptionCode.WCSTInvalidXML, EXCEPTION_TEXT.replace("$PARENT_ELEMENT", parentElement).replace("$CHILD_ELEMENT", childElement));
    }

    private static final String EXCEPTION_TEXT =  "$PARENT_ELEMENT element must contain exactly one $CHILD_ELEMENT element.";
}