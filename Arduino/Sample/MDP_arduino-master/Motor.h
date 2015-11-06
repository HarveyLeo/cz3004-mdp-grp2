/*
Motor Header File
- Variables Declaration
- Edited on 25 Feb
*/
#ifndef Motor_h
#define Motor_h

#include "Arduino.h"

//Motor Encoders
#define motor1_A       3
#define motor1_B       5
#define motor2_A       13
#define motor2_B       11

//Motor Pins
#define motor1_InA1    2          // INA motor pin
#define motor1_InB1    4          // INB motor pin 
#define motor1_PWM1    9          // PWM motor pin
#define motor2_InA1    7          // INA motor pin
#define motor2_InB1    8          // INB motor pin 
#define motor2_PWM1    10         // PWM motor pin

#define MAX_SPEED 255
#define MIN_SPEED 180

static unsigned int motor1_encoder = 0;
static unsigned int motor2_encoder = 0;

static int motor1_Aold = 0;
static int motor1_Bnew = 0;
static int motor2_Aold = 0;
static int motor2_Bnew = 0;

static double setpoint1 = 180, setpoint2 = 180;
static double input1 = motor1_encoder, output1 = 0;
static double input2 = motor2_encoder, output2 = 0;

double count_L = 0, count_R = 0, old_L = 0, old_R = 0;
int error = 0;
int count = 0;

const float motor1_forward_kp = 0.85;
const float motor1_forward_ki = 1.25;
const float motor1_forward_kd = 1.1;

const float motor2_forward_kp = 0.85;
const float motor2_forward_ki = 1.15;
const float motor2_forward_kd = 1.05;

#endif





