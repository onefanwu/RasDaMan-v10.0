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

/* SOURCE: ClientPingHandler.cc
 * MODULE:  communication
 * CLASS:   ClientPingHandler
 *
 * COMMENTS:
 *      The ClientPingHandler responds to a Ping message from the server with a Pong message.
 *
 */

#include "../../../common/src/logging/easylogging++.hh"

#include "../common/zmqutil.hh"
#include "../exception/unsupportedmessageexception.hh"

#include "clientpinghandler.hh"

namespace rasnet
{
using std::runtime_error;

ClientPingHandler::ClientPingHandler(zmq::socket_t& socket):
    socket(socket)
{
}

ClientPingHandler::~ClientPingHandler()
{
}

bool ClientPingHandler::canHandle(const std::vector<boost::shared_ptr<zmq::message_t> > &messages)
{
    MessageType messageType;

    if(messages.size() == 1
            && messageType.ParseFromArray(messages[0]->data(), messages[0]->size())
            && messageType.type() == MessageType::ALIVE_PING)
    {
        return true;
    }
    else
    {
        return false;
    }
}

void ClientPingHandler::handle(const std::vector<boost::shared_ptr<zmq::message_t> > &message)
{
    if (this->canHandle(message))
    {
        if(!ZmqUtil::sendCompositeMessage(socket, MessageType::ALIVE_PONG))
        {
            LERROR<<"Failed to send pong message to server.";
        }
    }
    else
    {
        throw UnsupportedMessageException();
    }
}

}