from interface import *
from arduino import *
from android import *
from pc import *
from config import *

import Queue
import thread
import threading
import os

class Main:

	def __init__(self):
		#allow rpi bluetooth to be discoverable
		os.system("sudo hciconfig hci0 piscan")
		
		#initialize connections
		self.bt = Android()
		self.ard = Arduino()
		self.pc = Pc()
		self.bt.connect(UUID)
		self.ard.connect()
		self.coord = 0
		self.mode = 0

		#initialize queues
		self.btQueue = Queue.Queue(maxsize=0)
		self.ardQueue = Queue.Queue(maxsize=0)
		self.pcQueue = Queue.Queue(maxsize=0)

		print "==========================="
		print "===Starting Transmission==="
		print "==========================="

	#read/write from Android (Bluetooth)
	def readBt(self,pcQueue):
		while 1:
			msg = self.bt.read()
			pcQueue.put_nowait(msg)
			print "Read from BT: %s\n" %msg
			

	def writeBt(self,btQueue):
		while 1:
			if not btQueue.empty():
				msg = btQueue.get_nowait()
				self.bt.write(msg)
				print "Write to BT: %s\n" %msg


	#read/write from Arduino (Serial comm)
	def readArd(self,pcQueue):
		while 1:
			msg = self.ard.read()
			pcQueue.put_nowait(msg)
			print "Read from Ard: %s\n" %msg

	def writeArd(self,ardQueue):
		while 1:
			if not ardQueue.empty():
				msg = ardQueue.get_nowait()
				self.ard.write(msg)
				print "Write to Ard: %s\n" %msg


	#read/write from PC (Serial comm)
	def readPc(self,ardQueue,btQueue):
		while 1:
			msg = self.pc.read()
		#	if msg=="E|":
		#		msg="edmund\n"
		#		self.pcQueue.put_nowait(msg)
			print "Read from PC: %s\n" %msg
			msg = msg.split("|")
			ardQueue.put_nowait(msg[0])
#			btQueue.put_nowait(msg[1])
		#	ardQueue.put_nowait(msg[0])
		#	print "Write to Ard: %s\n" %msg[1]
		#	print "Read from PC: %s\n" %(msg[0],msg[1])

	def writePC(self,pcQueue):
		while 1:
			if not pcQueue.empty():
				msg =pcQueue.get_nowait()
			#	len1=len(msg)
			#	msg= "hello world"
				self.pc.write(msg)
				print "Write to PC -: %s\n" %msg
			#	print "Length: %d\n" %len1 

	#multithreading 
	def multithread(self):
		try:
			thread.start_new_thread(self.readBt, (self.pcQueue,))
			thread.start_new_thread(self.readArd, (self.pcQueue,))
			thread.start_new_thread(self.readPc, (self.ardQueue,self.btQueue))
			thread.start_new_thread(self.writeBt, (self.btQueue,))
			thread.start_new_thread(self.writeArd, (self.ardQueue,))
			thread.start_new_thread(self.writePC, (self.pcQueue,))

		except Exception, e:
 			print "Error in threading: %s" %str(e)

 		while 1:
 			pass

 	def disconnectAll(self):
 		try:
 			self.bt.disconnect()
 			self.ard.disconnect()
 			self.pc.disconnect()
 		except Exception, e:
 			pass

 	def getCoord(self):
 		while self.coord == 0:
 			self.coord = self.bt.read()
 		self.pcQueue.put_nowait(self.coord)
 		print "Coordinates received: %s\n" %self.coord

 	def getMode(self):
 		#mode = 1 for exploration, mode = 2 for fast
 		while mode == 0:
 			self.msg = self.bt.read()
 			if msg == "exp":
 				mode = 1
 				pcQueue.put_nowait("explore")
 			else:
 				mode = 2
 				pcQueue.put_nowait("fast")

try:
	while True:
		test = Main()
#		test.getCoord()
#		test.getMode()
		test.multithread()
except KeyboardInterrupt:
	print "Terminating the program now..."
#	self.disconnectAll()
#	pass

