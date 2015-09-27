from bluetooth import *
from interface import *
import time
 
class Android(interface):
  #  def __init__(self):
    
    def connect(self):
        try:
            self.server_sock=BluetoothSocket( RFCOMM )
            #self.server_sock.allow_reuse_address = True
            #self.server_sock = self.server_socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            #self.server_sock.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR,1)
            self.server_sock.bind(("",5))
            self.server_sock.listen(5)
            port = self.server_sock.getsockname()[1]

            uuid = "661dd0dc-e688-4945-9921-6b13ba67b07e"

            advertise_service( self.server_sock, "SampleServer",
                               service_id = uuid,
                               service_classes = [ uuid, SERIAL_PORT_CLASS ],
                               profiles = [ SERIAL_PORT_PROFILE ], 
#                                      protocols = [ OBEX_UUID ] 
                                )

            print "Waiting for connection on RFCOMM channel %d" % port
            self.btsock, client_info = self.server_sock.accept()
            secure = client_info[0]
          
            if secure != '08:60:6E:A5:A4:1E':
                print "Unauthorized device, disconnecting..."
                return 0
                
            print "Accepted connection from ", client_info
            print "Connected to Android!"
            return 1
        except Exception, e:
            print "Bluetooth connection error -- error: %s" %str(e)
            try:
                print "%s" %str(x)
                self.btsock.close()
                self.server_sock.close()
            except:
                print "Error"
            return 0
         
    def disconnect(self):
        try:
                self.btsock.close()
                self.server_sock.close()
        except Exception, e:
            print "Bluetooth disconnection error -- error: %s" %str(e)
        
    def writetoBT(self,msg):
                try:
                    self.btsock.send(msg)
                    print "Write to Android: %s" %(msg)
                except Exception, e:
                    print "Bluetooth write error -- error: %s" %str(e)

                    connected = 0
                    connected = self.connect()
                    while connected == 0:
                        print "Attempting reconnection..."
                        #self.disconnect()
                        time.sleep(1)
                        connected = self.connect()
 
    def readfromBT(self):
                try:
                    msg = self.btsock.recv(1024)
                    print "Read from Android: %s" %(msg)
                    return (msg)
                except Exception, e:
                    print "Bluetooth read error -- error: %s" %str(e)
                    
                    connected = 0
                    connected = self.connect()
                    while connected == 0:
                        print "Attempting reconnection..."
#                        self.disconnect()
                        time.sleep(1)
                        connected = self.connect()
