/* 
 * File:   qtpolygonutil.hh
 * Author: bbell
 *
 * Created on September 11, 2017, 4:50 PM
 */

#ifndef QTPOLYGONUTIL_HH
#define	QTPOLYGONUTIL_HH

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

#include <string>

#include "qlparser/qtclippingutil.hh"
#include "raslib/point.hh"
#include "raslib/mddtypes.hh"
#include "raslib/error.hh"

#include <vector>
#include <map>

using namespace std;

/*
 * Get the bounding box of the given polygon i.e. the point with min coordinates on all axes and the point
 * with max coordinates on all axes
 */
pair<r_Point, r_Point> getBoundingBox(const vector<r_Point>& polygon);

/*
 *  Lars Linsen - Graphics and Visualization course : Lecture 08
 * http://www.faculty.jacobs-university.de/llinsen/teaching/320322/Lecture08.pdf
 */
//modify the mask for marking the edges of the polygon
void rasterizePolygon( vector< vector<char> >& mask, const vector< r_Point >& polygon );
//modify a mask for marking the points inside a polygon
//this works by checking the neighbours of each point in the boundary drawn by rasterizePolygon, and verifies which side is interior, calling floodFillFromPoint on the interior value.
//optimized by first checking that the cell is not already filled, and then checking if it is on the interior or exterior of the polygon.
void polygonInteriorFloodfill( vector< vector< char > >& mask, const vector< r_Point >& polygon);
//modify a mask for flood filling with value oldColor changed to value newColor from point mask[y][x]
void floodFillFromPoint( vector< vector< char > >& mask, size_t x, size_t y, char oldColor, char newColor );


/*
 *  From geeksforgeeks
 * To find orientation of ordered triplet (p, q, r).
 * The function returns following values
 * 0 --> p, q and r are colinear
 * 1 --> Clockwise
 * 2 --> Counterclockwise
 * !! All functions are defined only for 2D points
 */
int orientation(const r_Point& p, const r_Point& q, const r_Point& r);
    
int orientation(const r_Point& p, const r_Point& q, const double x, const double y);
    
int orientation(const vector< double >& p, const vector< double >& q, const vector< double >& r);

/*
 * Checks if the given point is inside or outside the given polygon
 * returns the number of segments that the line sent from the point in the direction of OX and parallel with it intersects
 * if the number is even then the point is outside; otherwise it is inside
 * !! All functions are defined only for 2D points
 * This function (with double x and double y as parameters) is not supposed to work when the line sent from the point
 * intersects another vertex of the polygon
 */ 
int checkPointInsidePolygon( const double x, const double y, const vector< r_Point >& polygon );
    
int checkPointInsidePolygon( const r_Point& x, const vector< r_Point >& polygon );
    
int checkPointInsidePolygon( const vector< double >& x, const vector< vector< double > >& polygon );

// takes an integer lattice point and a polygon, and verifies whether or not the vertex is in the polygon.
bool isPointInsidePolygon( const int testx, const int testy, const vector< r_Point >& polygon);

/*
 * http://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/
 * checks if the segments (p1,q1) and (p2,q2) intersect or not
 * !! It is only defined for 2D
 */ 
bool checkSegmentsIntersect( const r_Point& p1, const r_Point& q1, const r_Point& p2, const r_Point& q2 );

/*
 * Computes the mask corresponding to the given polygon and the given bounding box by using divide&conquer method
 * checks if the current square/rectangle (with the diagonal vertices v1 and v2) intersects our polygon
 * if it does, then it is split in 4 squares and this function is repeated
 * otherwise we don't consider the respective square
 */ 
void checkSquare( const r_Point& v1, const r_Point& v2, const vector< r_Point >& polygon, vector< vector< char > >& mask );

/*
 * Changes the point X given in 3D to a 2D inside the plane generated by the polygon
 * The point X and the polygon are given in 3D and the point X should be inside the plane generated by the polygon
 */
vector<double> changePointTo2D( const r_Point& x, const vector< r_Point >& polygon );

vector<double> changePointTo2D( const vector< double >& x, const vector< r_Point >& polygon );

/*
 * Changes the vertices of the polygon that are given in 3D to corresponding 2D coordinates inside the 
 * plane generated by the polygon/
 */    
vector< vector<double> > changePolygonTo2D( const vector< r_Point >& polygon );

/*
 * Take v1 as the left-down vertex of the cube
 * and v2 as the right-up vertex of the cube !!!
 * 
 * Checks if the given cube with the opposite vertices v1 and v2 intersects the plane generated by the polygon or not
 * If it does, then the cube is split in 8 cubes and the procedure is repeated
 * 
 * If we find a point which is inside the polygon or at a distance smaller than 0.5 (on any of the axes) from the inside
 * of the polygon, then we add this point into the result map
 */ 


void checkCube( const r_Point& v1, const r_Point& v2, const vector< r_Point >& polygon, map< r_Point, bool, classcomp >& result );

#endif	/* QTPOLYGONUTIL_HH */

