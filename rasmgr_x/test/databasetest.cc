/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#include <string>
#include <boost/cstdint.hpp>

#include "../../common/src/unittest/gtest.h"
#include "../../common/src/mock/gmock.h"
#include "../../common/src/logging/easylogging++.hh"

#include "../../rasmgr_x/src/database.hh"

using rasmgr::Database;

TEST(DatabaseTest, increaseSessionCount)
{
	Database db("dbName");
	std::string clientId = "client";
	std::string sessionId = "session";

	ASSERT_FALSE(db.isBusy());

	ASSERT_NO_THROW(db.increaseSessionCount(clientId, sessionId));

	ASSERT_TRUE(db.isBusy());

	ASSERT_ANY_THROW(db.increaseSessionCount(clientId, sessionId));
}


TEST(DatabaseTest, decreaseSessionCount)
{
	Database db("dbName");
	std::string clientId = "client";
	std::string sessionId = "session";

	//Initial state
	ASSERT_FALSE(db.isBusy());

	//Increase the session count and then decrease it
	ASSERT_NO_THROW(db.increaseSessionCount(clientId, sessionId));

	ASSERT_TRUE(db.isBusy());

	int deletedSessions;
	ASSERT_NO_THROW(deletedSessions=db.decreaseSessionCount(clientId, sessionId));

	ASSERT_EQ(1, deletedSessions);

	ASSERT_FALSE(db.isBusy());
}

TEST(DatabaseTest, clearSessionCount)
{
	Database db("dbName");
	std::string clientId = "client";
	std::string sessionId = "session";

	//Initial state
	ASSERT_FALSE(db.isBusy());

	//Increase the session count and then clear it
	ASSERT_NO_THROW(db.increaseSessionCount(clientId, sessionId));

	ASSERT_TRUE(db.isBusy());

	ASSERT_NO_THROW(db.clearSessionCount());

	ASSERT_FALSE(db.isBusy());
}