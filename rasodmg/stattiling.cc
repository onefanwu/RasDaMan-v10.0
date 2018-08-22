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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/**
 * SOURCE: stattiling.cc
 *
 * MODULE: rasodmg
 * CLASS:  r_Stat_Tiling r_Access
 *
 * COMMENTS:
 *          None
 */

#ifdef __VISUALC__
// Diable warning for signed/unsigned mismatch.
#pragma warning( disable : 4018 )
#endif

#include "config.h"
#include "rasodmg/interesttiling.hh"
#include "rasodmg/alignedtiling.hh"
#include "rasodmg/stattiling.hh"

#include <assert.h>
#include <string.h>
#include <math.h>
#include <cstdlib>

#include <logging.hh>

// Uncoment the _VISUALIZE_2D_DECOMP_ line to generate ppm files the
// visualization of the domain decomposition done by the algoritm
// #define _VISUALIZE_2D_DECOMP_

// Uncomment the following line to have debug information (printfs)
// #define _DEBUG_STATTILING_

#ifdef _VISUALIZE_2D_DECOMP_
#include "tools/visualtiling2d.hh"
#endif

const char*
r_Stat_Tiling::description = "dimensions, access patterns, border threshold, interesting threshold, tile size (in bytes) (ex: \"2;[0:9,0:9],3;[100:109,0:9],2;2;0.3;100\")";

const
r_Area r_Stat_Tiling::DEF_BORDER_THR = 50L;
const r_Double
r_Stat_Tiling::DEF_INTERESTING_THR  = 0.20;

r_Stat_Tiling::r_Stat_Tiling(const char* encoded)
    :   r_Dimension_Tiling(0, 0)
{
    if (!encoded)
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << (encoded ? encoded : "NULL") << ")";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

    r_Dimension tileD = 0;
    std::vector<r_Access> vectAccessInfo;
    r_Access*    accessInfo = NULL;
    r_Minterval* accessInterv = NULL;
    r_ULong      accessTimes = 0;
    r_Bytes tileS = 0, lenToConvert = 0, lenInToConvert = 0;
    r_Area borderTH = 0;
    r_Double interestTH = 0.;
    const char* pStart = NULL, *pEnd = NULL, *pRes = NULL, *pTemp = NULL;
    char* pToConvert = NULL;
    const char* pInRes = NULL, *pInEnd = NULL;
    char* pInToConvert = NULL;

    pStart = encoded;
    pEnd = pStart + strlen(pStart);
    pTemp = pStart;

    pRes = strstr(pTemp, TCOLON);
    if (!pRes)
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding tile dimension from \"" << pTemp << "\".";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

//deal with dimension
    lenToConvert = pRes - pTemp;
    pToConvert = new char[lenToConvert + 1];
    memcpy(pToConvert, pTemp, lenToConvert);
    pToConvert[lenToConvert] = '\0';

    tileD = strtol(pToConvert, (char**)NULL, DefaultBase);
    if (!tileD)
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding tile dimension from \"" << pToConvert << "\", is not a number.";
        delete[] pToConvert;
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

//skip COLON && free buffer
    delete[] pToConvert;
    if (pRes != (pEnd - 1))
    {
        pRes++;
    }
    else
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding access information from \"" << pStart << "\", end of stream.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

//deal with access informations
    pTemp = pRes;
    pRes = strstr(pTemp, TCOLON);
    if (!pRes)
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding access information from \"" << pTemp << "\".";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

    while (pRes)
    {
        //is access info?
        if (*pTemp != *LSQRBRA)
        {
            break;
        }

        //copy substring in buffer
        lenToConvert = pRes - pTemp;
        pToConvert = new char[lenToConvert + 1];
        memcpy(pToConvert, pTemp, lenToConvert);
        pToConvert[lenToConvert] = '\0';

        //deal with access Interval
        pInEnd = pToConvert + strlen(pToConvert);
        pInRes = strstr(pToConvert, RSQRBRA);
        if (!pInRes)
        {
            LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding access information from \"" << pToConvert << "\".";
            delete[] pToConvert;
            throw r_Error(TILINGPARAMETERNOTCORRECT);
        }

        lenInToConvert = pInRes - pToConvert + 1; //1 for ]
        pInToConvert = new char[lenInToConvert + 1];
        memcpy(pInToConvert, pToConvert, lenInToConvert);
        pInToConvert[lenInToConvert] = '\0';

        try
        {
            accessInterv = new r_Minterval(pInToConvert);
            delete [] pInToConvert;
        }
        catch (r_Error& err)
        {
            LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding access interval \"" << pInToConvert << "\" from \"" << pToConvert << "\".";
            LERROR << "Error " << err.get_errorno() << " : " << err.what();
            delete[] pInToConvert;
            delete[] pToConvert;
            throw r_Error(TILINGPARAMETERNOTCORRECT);
        }

        //deal with access Times
        pInRes = strstr(pInRes, TCOMMA);
        if (!pInRes)
        {
            LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding access times \"" << pInRes << "\" from acess information \"" << pToConvert << "\", not specified.";
            delete[] pInToConvert;
            delete[] pToConvert;
            throw r_Error(TILINGPARAMETERNOTCORRECT);
        }
        if (pInRes != (pEnd - 1))
        {
            pInRes++;
        }
        else
        {
            LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding access times \"" << pInRes << "\" from acess information \"" << pToConvert << "\", not specified.";
            delete[] pInToConvert;
            delete[] pToConvert;
            throw r_Error(TILINGPARAMETERNOTCORRECT);
        }
        accessTimes = strtol(pInRes, (char**)NULL, DefaultBase);
        if (!accessTimes)
        {
            LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding access times \"" << pInRes << "\" from acess information \"" << pToConvert << "\", not a number.";
            delete[] pInToConvert;
            delete[] pToConvert;
            throw r_Error(TILINGPARAMETERNOTCORRECT);
        }

        accessInfo = new r_Access(*accessInterv, accessTimes);
        vectAccessInfo.push_back(*accessInfo);
        delete accessInfo;
        delete accessInterv;

        //skip COLON && free buffer
        delete[] pToConvert;

        if (pRes != (pEnd - 1))
        {
            pRes++;
        }
        else
        {
            LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding access informations, end of stream.";
            throw r_Error(TILINGPARAMETERNOTCORRECT);
        }

        //deal with next item
        pTemp = pRes;
        pRes = strstr(pTemp, TCOLON);
    }

    if (vectAccessInfo.empty())
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding access informations, no access informations specified.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

//deal with borderTH
    lenToConvert = pRes - pTemp;
    pToConvert = new char[lenToConvert + 1];
    memcpy(pToConvert, pTemp, lenToConvert);
    pToConvert[lenToConvert] = '\0';

    borderTH = static_cast<r_Area>(strtol(pToConvert, (char**)NULL, DefaultBase));
    if (!borderTH)
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding border threshold \"" << pToConvert << "\".";
        delete[] pToConvert;
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

//skip COLON && free buffer
    delete[] pToConvert;
    if (pRes != (pEnd - 1))
    {
        pRes++;
    }
    else
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding interesting threshold, end of stream.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

//deal with interestTH
    pTemp = pRes;
    pRes = strstr(pTemp, TCOLON);
    if (!pRes)
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding interesting threshold.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

//copy substring into buffer
    lenToConvert = pRes - pTemp;
    pToConvert = new char[lenToConvert + 1];
    memcpy(pToConvert, pTemp, lenToConvert);
    pToConvert[lenToConvert] = '\0';

    interestTH = strtod(pToConvert, (char**)NULL);
    if (!interestTH)
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding interesting threshold \"" << pToConvert << "\".";
        delete[] pToConvert;
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }
    if (interestTH < 0.)
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding interesting threshold \"" << pToConvert << "\", negative number.";
        delete[] pToConvert;
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

    if (interestTH > 1.)
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding interesting threshold \"" << pToConvert << "\", not in [0,1] interval.";
        delete[] pToConvert;
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

//skip COLON && free buffer
    delete[] pToConvert;
    if (pRes != (pEnd - 1))
    {
        pRes++;
    }
    else
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding tile size, end of stream.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

//deal with tilesize
    pTemp = pRes;
    tileS = strtol(pTemp, (char**)NULL, DefaultBase);
    if (!tileS)
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding tile size \"" << pToConvert << "\", not a number.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }
    if (tileS < 0.)
    {
        LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << encoded << "): Error decoding tile size \"" << pToConvert << "\", negative number.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

    border_thr = borderTH;
    stat_info = vectAccessInfo;
    interesting_thr = interestTH;
    dimension = tileD;
    tile_size = tileS;
}


r_Stat_Tiling::r_Stat_Tiling(r_Dimension dim, const std::vector<r_Access>& stat_info2, r_Bytes ts, r_Area border_threshold, r_Double interesting_threshold)
    :   r_Dimension_Tiling(dim, ts),
        interesting_thr(interesting_threshold),
        border_thr(border_threshold),
        stat_info(stat_info2)
{
    // Filter accesses all areas have the same dimension if successfull else exception
    filter(stat_info);
    LTRACE << "done\n";
    std::vector<r_Access>::iterator areas_it = stat_info.begin();
    // Count total accesses
    r_ULong total_accesses = 0;



    for (; areas_it != stat_info.end(); areas_it++)
    {
        if ((*areas_it).get_pattern().dimension() != dim)
        {
            LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << dim << ", " << &stat_info
                   << ", " << ts << ", " << border_threshold << ", " << interesting_threshold
                   << ") dimension (" << dim << ") does not match dimension of access patterns ("
                   << (*areas_it).get_pattern().dimension() << ")";
            throw r_Edim_mismatch(dim, (*areas_it).get_pattern().dimension());
        }
        total_accesses += (*areas_it).get_times();
    }

    LTRACE << "Defining interest areas... ";

    // Mininum number of accesses for being interesting
    r_ULong critical_accesses = static_cast<r_ULong>(interesting_thr * total_accesses);

    iareas.clear();
    for (areas_it = stat_info.begin(); areas_it != stat_info.end(); areas_it++)
    {
        if ((*areas_it).get_times() >= critical_accesses) // Threshold exceeded or equal
        {
            iareas.push_back(areas_it->get_pattern());           // count this area in
        }
    }
}

r_Stat_Tiling::~r_Stat_Tiling()
{
}

r_Tiling* r_Stat_Tiling::clone() const
{
    r_Tiling* copy = new r_Stat_Tiling(dimension, stat_info, tile_size, border_thr, interesting_thr);
    return copy;
}

void r_Stat_Tiling::print_status(std::ostream& os) const
{
    os << "r_Stat_Tiling[ ";
    r_Dimension_Tiling::print_status(os);
    os << " border threshold = " << border_thr << ", interesting threshold = " << interesting_thr << " ]";
}

const std::vector<r_Minterval>&
r_Stat_Tiling::get_interesting_areas() const
{
    return iareas;
}

r_Tiling_Scheme
r_Stat_Tiling::get_tiling_scheme() const
{
    return r_StatisticalTiling;
}

r_Area
r_Stat_Tiling::get_border_threshold() const
{
    return border_thr;
}

r_Double
r_Stat_Tiling::get_interesting_threshold() const
{
    return interesting_thr;
}

r_Access
r_Stat_Tiling::merge(const std::vector<r_Access>& patterns) const
{
    // Create an interator for list of patterns
    std::vector<r_Access>::const_iterator it = patterns.begin();

    // The result (initialy updated to the first element of patterns)
    r_Access result = (*it);
    it++;

    // For all patterns
    for (; it != patterns.end(); it++)
    {
        result.merge_with(*it);                          // Merge them
    }
    return result;                                     // Return the result
}

void r_Stat_Tiling::filter(std::vector<r_Access>& patterns) const
{
    // List to hold the result
    std::vector<r_Access> result;

    // List to hold the clusters
    std::vector<r_Access> cluster;

    // Iterators for pattern and cluster list
    std::vector<r_Access>::iterator pattern_it = patterns.begin();
    std::vector<r_Access>::iterator cluster_it;

    // For all elements in pattern table
    while (!patterns.empty())
    {
        // Clean cluster
        cluster.clear();
        // Cluster with first element of pattern list
        cluster.push_back(patterns.back());
        patterns.pop_back();

        // For all elements in the cluster
        for (cluster_it = cluster.begin(); cluster_it != cluster.end(); cluster_it++)
        {
            // For all remaining patterns
            for (pattern_it = patterns.begin(); pattern_it != patterns.end(); pattern_it++)
            {
                // Pattern near an element from the cluster
                if ((*cluster_it).is_near(*pattern_it, border_thr))
                {
                    // Add pattern to the cluster
                    cluster.push_back(*pattern_it);
                    // Remove pattern from list
                    patterns.erase(pattern_it);
                }
            }
        }
        // Merge cluster and add to result
        result.push_back(merge(cluster));
    }
    // Filtered table
    patterns = result;
}

std::vector<r_Minterval>*
r_Stat_Tiling::compute_tiles(const r_Minterval& domain, r_Bytes typelen) const
{
    r_Dimension num_dims = domain.dimension();                   // Dimensionality of dom
    if (domain.dimension() != dimension)
    {
        LERROR << "r_Stat_Tiling::compute_tiles(" << domain << ", " << typelen << ") dimension (" << dimension << ") does not match dimension of object to tile (" << num_dims << ")";
        throw r_Edim_mismatch(dimension, num_dims);
    }
    if (typelen > tile_size)
    {
        LERROR << "r_Stat_Tiling::compute_tiles(" << domain << ", " << typelen << ") tile size (" << tile_size << ") is smaller than type length (" << typelen << ")";
        throw r_Error(TILESIZETOOSMALL);
    }
#ifdef _VISUALIZE_2D_DECOMP_                           // User wants a visual
    static int count;                                    // Number of decomps
    ++count;                                             // Update num decomps
    // of the 2D decomp.
    Visual_Tiling_2D* vis;
    if (domain.dimension() == 2)
    {
        // Create an object for visualization
        char fname[80];
        sprintf(fname, "2D_decomp_stat_%d.ppm", count);
        vis = new Visual_Tiling_2D(domain, fname);
    }
#endif

    // *** Main algoritm ***

    std::vector<r_Minterval>* result;                          // Holds the result

    if (iareas.empty())                               // No interest areas
    {
        // Perform regular tiling

        result = r_Size_Tiling::compute_tiles(domain, typelen);
    }
    else                                                 // We have interest areas
    {
        // Use interest areas for tiling the domain

        r_Interest_Tiling* tiling = NULL;

        try
        {
            tiling = new r_Interest_Tiling(dimension, iareas, get_tile_size(), r_Interest_Tiling::SUB_TILING);
            result = tiling->compute_tiles(domain, typelen);
            delete tiling;
        }
        catch (r_Error& err)
        {
            if (tiling)
            {
                delete tiling;
            }
            throw;
        }
    }

    // *** End of the main algorithm

#ifdef _VISUALIZE_2D_DECOMP_
    if (domain.dimension() == 2)
    {
        vis->set_pen(0, 255, 255);

        std::vector<r_Minterval>::iterator it(*result);
        it.seek_begin();
        for (; it.not_done(); it++)
        {
            (*vis) << (*it);
        }

        vis->set_pen(255, 255, 0);

        std::vector<r_Minterval>& ia = (std::vector<r_Minterval>) iareas; // Cast away constness

        std::vector<r_Minterval>::iterator it2(ia);
        it2.seek_begin();
        for (; it2.not_done(); it2++)
        {
            (*vis) << (*it2);
        }

        delete vis;
    }
#endif

    // Return result

    return result;
}



//***************************************************************************

r_Access::r_Access() :
    times(0)
{
}

r_Access::r_Access(const r_Minterval& pattern, r_ULong accesses) :
    interval(pattern), times(accesses)
{
}

const r_Minterval& r_Access::get_pattern() const
{
    return interval;
}

void r_Access::set_pattern(const r_Minterval& pattern)
{
    interval = pattern;
}

r_ULong r_Access::get_times() const
{
    return times;
}

void r_Access::set_times(r_ULong accesses)
{
    times = accesses;
}

bool r_Access::is_near(const r_Access& other, r_ULong border_threshold) const
{
    const r_Minterval& a = this->interval;
    const r_Minterval& b = other.interval;

    r_Dimension num_dims = interval.dimension();
    if (num_dims != b.dimension())
    {
        LERROR << "r_Access::is_near(" << other << ", " << border_threshold << ") parameter 1 does not match my dimension (" << num_dims << ")";
        throw r_Edim_mismatch(num_dims, b.dimension());
    }
    bool the_same = true;

    // For all dimensions
    for (r_Dimension i = 0; i < num_dims; i++)
    {
        // Higher limit does not exceed border threshold
        if (labs(a[i].high() - b[i].high()) > border_threshold)
        {
            the_same = false;
            break;
        }

        // Lower limit does not exceed border threshold
        if (labs(a[i].low() - b[i].low()) > border_threshold)
        {
            the_same = false;
            break;
        }
    }

    return the_same;
}

void r_Access::merge_with(const r_Access& other)
{
    interval.closure_with(other.interval);
    times += other.times;
}

void r_Access::print_status(std::ostream& os) const
{
    os << "{" << times << "x: " << interval << "}";
}

std::ostream& operator<<(std::ostream& os, const r_Access& access)
{
    access.print_status(os);

    return os;
}

bool r_Access::operator==(const r_Access& other) const
{
    return ((this->interval == other.interval) && (this->times == other.times));
}

bool r_Access::operator!=(const r_Access& other) const
{
    return !(*this == other);
}
