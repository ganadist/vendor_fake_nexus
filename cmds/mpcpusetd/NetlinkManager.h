// vim: ts=4 sw=4 expandtab

#ifndef _NETLINKMANAGER_H
#define _NETLINKMANAGER_H

#include <sysutils/NetlinkListener.h>

class NetlinkHandler;

class NetlinkManager {
private:
    static NetlinkManager *sInstance;

private:
    NetlinkHandler       *mHandler;
    int                  mSock;

public:
    virtual ~NetlinkManager();

    int start();
    int stop();

    static NetlinkManager *Instance();

private:
    NetlinkManager();
};
#endif

