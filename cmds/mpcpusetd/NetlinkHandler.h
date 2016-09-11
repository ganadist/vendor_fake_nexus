// vim: ts=4 sw=4 expandtab
#ifndef __NETLINKHANDLER_H__
#define __NETLINKHANDLER_H__

#include <sysutils/NetlinkListener.h>
#include <sysutils/NetlinkEvent.h>

class NetlinkHandler: public NetlinkListener {
public:
    NetlinkHandler(int listenerSocket);
    virtual ~NetlinkHandler();

    int start(void);
    int stop(void);

protected:
    virtual void onEvent(NetlinkEvent *event);
};

#endif
