#!/usr/bin/python
# -*- coding: utf-8 -*-
import os, sys
import serial
import time

from config import *
from interface import *

class Arduino(interface):

	def __init__(self):
		self.baudrate = BAUD
		self.ser = 0

	def connect(self,port):
		#connect to serial port
	    try:
	    	print "-------------------------------"
	    	print "Trying to connect to Arduino..."
			self.ser = serial.Serial(port,self.baudrate, timeout=3)
			time.sleep(1)

			if(self.ser != 0):
				print "Connected to Arduino!"
				self.read()
			return 1

	    except Exception, e:
			print "Arduino connection exception: %s" %str(e)
			return 0

	def write(self,msg):
		try:
			self.ser.write(msg + "|") #write msg with | as end of msg
		except Exception, e:
			print "Arduino write exception: %s" %str(e)

	def read(self):
		try:
			msg = self.ser.readline() #read msg from arduino sensors
			return msg
		except Exception, e:
			print "Arduino read exception: %s" %str(e)