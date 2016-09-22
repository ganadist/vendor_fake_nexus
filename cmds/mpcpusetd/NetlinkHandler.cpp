// vim: ts=4 sw=4 expandtab
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

#define LOG_NDEBUG 0
#define LOG_TAG "NetlinkHandler"
#include <cutils/log.h>
#include <sysutils/NetlinkEvent.h>

#include "NetlinkHandler.h"
#include "CpusetManager.h"

NetlinkHandler::NetlinkHandler(int listenerSocket) :
    NetlinkListener(listenerSocket) {
}

NetlinkHandler::~NetlinkHandler() {
}

int NetlinkHandler::start() {
    return this->startListener();
}

int NetlinkHandler::stop() {
    return this->stopListener();
}

void NetlinkHandler::onEvent(NetlinkEvent *event) {
    const char * subsys = event->getSubsystem();
    if (!subsys) {
        return;
    }
    CpusetManager *cm = CpusetManager::Instance();
    if (!strcmp(subsys, "cpu")) {
        cm->handleCpuEvent(event);
    }
}
