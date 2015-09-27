from interface import *
from arduino import *
from android import *

import queue
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
		self.bt.connect()
		self.ard.connect()

		#initialize queues
		self.btQueue = Queue.Queue(maxsize=0)
		self.ardQueue = Queue.Queue(maxsize=0)

		#for testing purposes
		self.btQueue.put("hey")
		self.btQueue.put("yo")
		self.btQueue.put("aaaaaaaaa")
		self.btQueue.put("dd")
		self.btQueue.put("dd")
		self.btQueue.put("aa")
		self.ardQueue.put("A081")
		self.ardQueue.put("W1")
		# self.ardQueue.put("D081")
		# self.ardQueue.put("W1")
		self.ardQueue.put("D54")
		self.ardQueue.put("A54")
		self.btQueue.put("ZZZ")

	#read/write from Android (Bluetooth)
	def readBt(self,ardQueue):
		while 1:
			msg = self.bt.readfromBT()
			if msg == 'apple':
				ardQueue.put_nowait("A801")
			print "Read from BT: %s\n" %msg

	def writeBt(self,btQueue):
		while 1:
			if not btQueue.empty():
				msg = btQueue.get_nowait()
				self.bt.writetoBT(msg)

	#read/write from Arduino (Serial comm)
	def readArd(self,btQueue):
		while 1:
			msg = self.ard.read()
			btQueue.put_nowait(msg)
			print "Read from Ard: %s\n" %msg

	def writeArd(self,ardQueue):
		while 1:
			if not ardQueue.empty():
				msg = ardQueue.get_nowait()
				self.ard.write(msg)


	#multithreading 
	def multithread(self):
		try:
			thread.start_new_thread(self.readBt, (self.ardQueue,))
			thread.start_new_thread(self.readArd, (self.btQueue,))
			thread.start_new_thread(self.writeBt, (self.btQueue,))
			thread.start_new_thread(self.writeArd, (self.ardQueue,))

		except Exception, e:
 			print "Error in threading -- Error: %s" %str(e)

 		while 1:
 			pass


test = Main()
test.multithread()