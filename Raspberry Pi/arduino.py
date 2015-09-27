#!/usr/bin/python
# -*- coding: utf-8 -*-
import os, sys
import serial
import time

from interface import *

class Arduino(interface):

	def __init__(self):
		self.baudrate = 115200
		ser = 0

	def connect(self):
		global ser
		locations=['/dev/ttyACM0','/dev/ttyACM1']

		#connect to serial port
		for device in locations:
		    try:
			ser = serial.Serial(device,self.baudrate, timeout=3)
			print "Serial = %s" %str(ser)
			time.sleep(1)

			if(ser != 0):
				print "Connected to Arduino! - "
				self.read()
				return 1
		    except Exception, e:
			print "Arduino Connection Error: %s" %str(e)
			return 0

	def write(self,msg):
		global ser

		msg = msg + "|" #write msg with | as end of msg
		ser.write(msg)
		
		print "RPi: %s" %msg

	def read(self):
		global ser

		msg = ser.readline() #read msg from arduino sensors
		while (msg):
			print "Arduino: %s" %msg
			msg = ser.readline()
		return msg
