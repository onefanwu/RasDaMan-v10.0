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
package org.rasdaman.domain.cis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import petascope.util.BigDecimalUtil;

/**
 * 1 Coverage (1 Rasdaman collection) can contain multiple downscaled
 * collections. e.g: test_mean_summer_airtemp has: test_mean_summer_airtemp_2
 * (downscaled by 2), test_mean_summer_airtemp_4 (downscaled by 4),...
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */

/**
 * NOTE: in rasdaman v10, this class is used for migrating to the new coverage pyramid model
 */
@Deprecated
@Entity
@Table(name = RasdamanDownscaledCollection.TABLE_NAME)
public class RasdamanDownscaledCollection implements Comparable<RasdamanDownscaledCollection>, Serializable {

    public static final String TABLE_NAME = "rasdaman_down_scaled_collection";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    @Column(name = "collection_name")
    private String collectionName;
    
    @Column(name = "level")
    private String level;

    public RasdamanDownscaledCollection() {

    }

    public RasdamanDownscaledCollection(String collectionName, BigDecimal level) {
        this.collectionName = collectionName;
        this.level = BigDecimalUtil.stripDecimalZeros(level).toPlainString();
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public BigDecimal getLevel() {
        return BigDecimalUtil.stripDecimalZeros(new BigDecimal(level));
    }

    public void setLevel(BigDecimal level) {
        this.level = BigDecimalUtil.stripDecimalZeros(level).toPlainString();
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }
        if (!(o instanceof RasdamanDownscaledCollection)) {
            return false;
        }
        RasdamanDownscaledCollection obj = (RasdamanDownscaledCollection) o;
        return obj.getCollectionName().equals(this.collectionName)
                && BigDecimalUtil.stripDecimalZeros(obj.getLevel()).equals(new BigDecimal(this.level));
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.getCollectionName().hashCode();
        result = 31 * result + new BigDecimal(this.level).intValue();
        
        return result;
    }

    @Override
    public int compareTo(RasdamanDownscaledCollection inputObject) {
        int result = -1;        
        if (inputObject != null) {
            BigDecimal inputLevel = inputObject.getLevel();
            if (this.level != null && inputLevel != null) {
                result = new BigDecimal(this.level).subtract(inputLevel).intValue();
            }
        }
        
        return result;        
    }
}
