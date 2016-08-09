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
package petascope.wcs2.handlers;

import java.util.List;
import static petascope.wcs2.extensions.FormatExtension.MIME_XML;

/**
 * Bean holding the response from executing a request operation.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class Response {

    private final List<byte[]> data;
    private final String[] xml;
    private final int exit_code;
    // true if the response was generated by a ProcessCoverage request
    private boolean processCoverage;
    // true if the response was requested in multipart
    private boolean multiPart;
    // formatType of encoding in WCPS query
    private String formatType;
    // if formatType is not text (gml, xml, text) then it needs a file name from coverageID to set when download WCS, WCPS result
    private String coverageID;

    private final String DEFAULT_COVERAGE_ID = "ows";
    private static final int DEFAULT_CODE = 200;

    // constructrs
    public Response(List<byte[]> data) {
        this(data, null, null, DEFAULT_CODE);
    }

    public Response(List<byte[]> data, int code) {
        this(data, null, null, code);
    }

    public Response(String[] xml) {
        this(null, xml, null); //FormatExtension.MIME_GML);
    }

    public Response(String xml[], int code) {
        this(null, xml, MIME_XML, code);
    }

    public Response(List<byte[]> data, String[] xml, String mimeType) {
        this(data, xml, mimeType, DEFAULT_CODE);
    }

    public Response(List<byte[]> data, String[] xml, String formatType, String coverageID) {
        this.data = data;
        this.xml = xml;
        this.formatType = formatType;
        this.coverageID = coverageID;
        this.exit_code = DEFAULT_CODE;
    }

    public Response(List<byte[]> data, String[] xml, String formatType, Boolean processCoverage) {
        this.data = data;
        this.xml = xml;
        this.formatType = formatType;
        this.processCoverage = processCoverage;
        this.exit_code = DEFAULT_CODE;
    }

    public Response(List<byte[]> data, String[] xml, String formatType, Boolean processCoverage, Boolean multiPart, String coverageID) {
        this.data = data;
        this.xml = xml;
        this.formatType = formatType;
        this.processCoverage = processCoverage;
        this.multiPart = multiPart;
        this.exit_code = DEFAULT_CODE;
        this.coverageID = coverageID;
    }

    public Response(List<byte[]> data, String[] xml, String mimeType, int code) {
        this.data = data;
        this.xml = xml;
        this.formatType = mimeType;
        this.exit_code = code;
    }

    // interface
    public List<byte[]> getData() {
        return data;
    }

    // Encoding in Rasql
    public String getFormatType() {
        if(formatType == null) {
            formatType = "";
        }

        return formatType;
    }

    public String[] getXml() {
        return xml;
    }

    public int getExitCode() {
        return exit_code;
    }

    public String getCoverageID() {
        // When coverageID is null, the request will return result with this filename as default.
        coverageID = (coverageID != null) ? coverageID : DEFAULT_COVERAGE_ID;
        return coverageID;
    }

    public boolean isProcessCoverage() {
        return processCoverage;
    }

    public void setProcessCoverage(boolean processCoverage) {
        this.processCoverage = processCoverage;
    }

    public boolean isMultiPart() {
        return multiPart;
    }

    public void setMultipart(boolean multiPart) {
        this.multiPart = multiPart;
    }

    public void setCoverageID(String coverageID) {
        this.coverageID = coverageID;
    }
}