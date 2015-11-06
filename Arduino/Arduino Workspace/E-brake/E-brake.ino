
/*---------------------------------------------------------------------------------------------------
                               MDP GROUP2 ARDUINO PROGRAM SOURCE CODES
                               ---------------------------------------
                               Done by Ahmad & Dominic
                               Last Updated: 29/9/2015 16:23 Hours

Robot movement is based on a 3x3 grid system.

Functions for access (Use CTRL + F and input MDPX,where X is function number, to zoom to function):

Main Functions:
MDP1 -> setup()
MDP2 -> loop()
MDP3 -> moveForward()
MDP4 -> rotateLeft()
MDP5 -> rotateRight()
MDP6 -> sendSensors()
  MDP6.1 -> sensorRead()
MDP7 -> frontAlignment()
  MDP7.1 -> angleAlign()
  MDP7.2 -> distAlign()
MDP8 -> turnLeft()
MDP9 -> turnRight()

Extension FunctionsL
MDP10 -> pidControlForward()
  MDP10.1 -> pidControlLR()
MDP11 -> insertionsort()
MDP12 -> distanceInCM()
MDP13 -> distanceInGrids()
MDP14 -> readyMsg()
MDP15 -> stopIfFault()
---------------------------------------------------------------------------------------------------*/
#include "DualVNH5019MotorShield.h"
#include "PinChangeInt.h"

DualVNH5019MotorShield md;

/*---------------------------------------------------------------------------------------------------
                                        Motor
---------------------------------------------------------------------------------------------------*/
#define motor_R_encoder 5  //Define pins for motor encoder input
#define motor_L_encoder 3

#define MAX_SPEED 370
#define SPEED 200

int right_encoder_val = 0, left_encoder_val = 0;
void RightEncoderInc(){right_encoder_val++;}
void LeftEncoderInc(){left_encoder_val++;}

/*---------------------------------------------------------------------------------------------------
                                        Sensor
---------------------------------------------------------------------------------------------------*/
#define LF A0   //Left-front
#define L  A5   //Left
#define CF  A2  //Centre-front
#define R  A3   //Right
#define RF A4   //Right-front
#define SR 0    //Short-range sensor
#define LR 1    //Long-range sensor

/*---------------------------------------------------------------------------------------------------
MDP1                                    Set-Up
---------------------------------------------------------------------------------------------------*/

void setup()
{
  Serial.begin(115200);
  md.init();
  PCintPort::attachInterrupt(motor_R_encoder, RightEncoderInc, CHANGE);
  PCintPort::attachInterrupt(motor_L_encoder, LeftEncoderInc, CHANGE);
}

/*---------------------------------------------------------------------------------------------------
MDP2                                  Main Program
---------------------------------------------------------------------------------------------------*/

void loop()
{
  char command_buffer[10];
  int i = 0, arg = 0, digit = 1;
  char newChar;
  left_encoder_val = 0; 
  right_encoder_val = 0; 

/*---------------------------------------------------------------------------------------------------
                               Establishing Serial Connection with RPi
---------------------------------------------------------------------------------------------------*/
  while (1){
    if (Serial.available()){
      newChar = Serial.read();
      command_buffer[i] = newChar;
      i++;
      if (newChar == '|'){
        i = 1;
        break;
      }
    }  
  }

  //First character in array is the command
  char command = command_buffer[0];
  
  //Converts subsequent characters in the array into an integer
   while(command_buffer[i] != '|'){
    arg = arg + ( digit * (command_buffer[i] - 48) );
    digit *= 10;
    i++;
  }

//command = 'W';
//arg=17;
/*---------------------------------------------------------------------------------------------------
                                        Input Commands
                                        --------------
LEGEND:
-------
W ---> Move Forward
A ---> Rotate Left
D ---> Rotate Right
E ---> Read Sensor Values
C ---> Recalibrate Robot's Center
T ---> Avoiding Obstacle In A Straight Line
L ---> Gradual Left Turn
R ---> Gradual Right Turn
---------------------------------------------------------------------------------------------------*/
  switch ( command ) {
  case 'W':
    {
      if(arg == 0) moveForward(1);
      else moveForward(arg); 
      readyMsg();
      break;
 
    }
    case 'A':
    {
      if(arg == 0) rotateLeft(90);
      else rotateLeft(arg);
      readyMsg();
      break;
    }
    case 'D':
    {
      if(arg == 0) rotateRight(90);
      else rotateRight(arg);
      readyMsg();
      break;
    }
    case 'E':
   {
      sendSensors();
      break;
   }
   case 'C':
   {
      frontAlignment();
      readyMsg();
      break;
   }
     case 'L':
   {
      turnLeft();
      readyMsg();
      break;
   }
     case 'R':
   {
      turnRight();
      readyMsg();
      break;
   }
    default:
   {
     break;
   }
    memset(command_buffer,0,sizeof(command_buffer));
  }
}
//End of loop()

/*---------------------------------------------------------------------------------------------------
MDP3                                    Move Forward
---------------------------------------------------------------------------------------------------*/
void moveForward(int distance){
  
  left_encoder_val = 0; 
  right_encoder_val = 0; 
  int pwmR = 375;
  int pwmL = 375;
  int output=0;
  int Roffset=10;
  int explore_flag = false;

  int multiplier;
  switch(distance){
    case 1: {
            pwmR = SPEED;
            pwmL = SPEED;
            multiplier = 550;
            explore_flag = true; 
            break;
    }
  case 2: multiplier = 565; break;
    case 3: multiplier = 575; break;
    case 4: multiplier = 580; break;
    case 5: multiplier = 580; break;
    case 6: multiplier = 580; break;
    case 7: multiplier = 580; break;
    case 8: multiplier = 580; break;
    case 9: multiplier = 580; break;
    case 10: multiplier = 590; break;
    case 11: multiplier = 590; break;
    case 12: multiplier = 590; break;
    case 13: multiplier = 590; break;
    case 14: multiplier = 590; break;
    case 15: multiplier = 590; break;
    case 16: multiplier = 590; break;
    case 17: multiplier = 590; break;
    default: multiplier = 590; break;
  
  }
  int target_Distance = multiplier * distance;
  
  while(1){
            if(explore_flag){
                  if(right_encoder_val > target_Distance){ // break
                    md.setBrakes(MAX_SPEED, MAX_SPEED);
                    delay(100);
                    md.setBrakes(0, 0);
                    break;
                  }
            }
            else{                          
                  if(right_encoder_val > target_Distance){ // break
                    md.setBrakes(MAX_SPEED, MAX_SPEED);
                    delay(300);
                    md.setBrakes(0, 0);
                    break;
                  }
            }
                  output = pidControlForward(left_encoder_val,right_encoder_val);
                  md.setSpeeds(pwmR-output,pwmL+output);
      }      
}
//End of moveForward()

/*---------------------------------------------------------------------------------------------------
MDP4                                      Rotate Left
---------------------------------------------------------------------------------------------------*/
int rotateLeft(int angle){
  left_encoder_val = 0;
  right_encoder_val = 0;
  int pwm1=200, pwm2=200, output=0;
  int angle_offset;

  if (angle <= 15){
    angle_offset = angle * 8.4;
    pwm1=150;
    pwm2=150;
  }
  else if ((angle > 15) && (angle <= 30))
    angle_offset = angle * 8.4;
  else if ((angle > 30) && (angle <= 45))
    angle_offset = angle * 9.0;   
  else if ((angle > 45) && (angle <= 90))   //8.9 When all fully charged (no pid speed 300)
    angle_offset = angle * 9.33;            //9.37 When all fully charged (pid speed 200)
                                            //9.33 When all fully charged, pid speed 200, no usb 5V
  else if ((angle > 90) && (angle <= 360))  
    angle_offset = angle * 8.9;
  else if ((angle > 360) && (angle <= 720))  
    angle_offset = angle * 8.9;
  else if ((angle > 720) && (angle <= 1080))
    angle_offset = angle * 8.9;

  while(1){

    if((left_encoder_val >= angle_offset - 70)&&(angle > 8)){ 
      md.setBrakes(400, 400);
      delay(100);
      md.setBrakes(0, 0);
      break;
    }


    if((left_encoder_val >= angle_offset - 5)&&(angle < 9)){
      md.setBrakes(400, 400);
      delay(100);
      md.setBrakes(0, 0);
      break;
    }
    output = pidControlLR(left_encoder_val,right_encoder_val);
    md.setSpeeds(pwm1-output, -(pwm2+output));
    
  }
}
//End of rotateLeft()


/*---------------------------------------------------------------------------------------------------
MDP5                                     Rotating Right
---------------------------------------------------------------------------------------------------*/
int rotateRight(int angle){
  left_encoder_val = 0;
  right_encoder_val = 0;
  int pwm1=200, pwm2=200, output=0;
  int angle_offset;

  if (angle <= 15){
    angle_offset = angle * 8.4;  
    pwm1=150;
    pwm2=150;
  }
  else if ((angle > 15) && (angle <= 30))
    angle_offset = angle * 8.4;  
  else if ((angle > 30) && (angle <= 45))
    angle_offset = angle * 8.9;
  else if ((angle > 45) && (angle <= 90)) //9.15 When all fully charged (no pid speed 300)
    angle_offset = angle * 9.33;          //9.37 When all fully charged (pid speed 200)
                                            //9.33 When all fully charged, pid speed 200, no usb 5V
  else if ((angle > 90) && (angle <= 360))  
    angle_offset = angle * 8.9;
  else if ((angle > 360) && (angle <= 720))  
    angle_offset = angle * 8.9;
  else if ((angle > 720) && (angle <= 1080)) 
    angle_offset = angle * 8.9;

  while(1){

    if((right_encoder_val >= angle_offset - 70)&&(angle > 8)){ 
      md.setBrakes(400, 400);
      delay(100);
      md.setBrakes(0, 0);
      break;
    }


    if((right_encoder_val >= angle_offset - 5)&&(angle < 9)){
      md.setBrakes(400, 400);
      delay(100);
      md.setBrakes(0, 0);
      break;
    }
    output = pidControlLR(left_encoder_val,right_encoder_val);
    md.setSpeeds(-(pwm1-output),(pwm2+output));
  }
}
//End of rotateRight()

/*---------------------------------------------------------------------------------------------------
MDP6                                 Sensor Reading Functions
---------------------------------------------------------------------------------------------------*/
//This function sends the sensor data to the RPi
void sendSensors(){
  int cmLF, cmCF, cmRF, cmL, cmR;
  cmLF = distanceInCM(sensorRead(20,LF),LF);
  cmCF = distanceInCM(sensorRead(20,CF),CF);
  cmRF = distanceInCM(sensorRead(20,RF),RF);
  cmL = distanceInCM(sensorRead(60,L),L);
  cmR = distanceInCM(sensorRead(20,R),R);
  
  Serial.print(":");
  Serial.print(distanceInGrids(cmLF,SR));
  Serial.print(":");
  //Serial.println(cmLF);
  Serial.print(distanceInGrids(cmCF,SR));
  Serial.print(":");
  //Serial.println(cmCF);
  Serial.print(distanceInGrids(cmRF,SR));
  Serial.print(":");
  //Serial.println(cmRF);
  Serial.print(distanceInGrids(cmL,LR));
  Serial.print(":");
  //Serial.println(cmL);
  Serial.print(distanceInGrids(cmR,SR));
  //Serial.print(":");
  //Serial.println(cmR);
  Serial.print("|\n");
}

//MDP6.1
//This function returns the average 10-bit ADC reading where n is the number of samples
int sensorRead(int n, int sensor){
  int x[n];
  int i;
  int sum = 0;
  for(i=0;i<n;i++){
    x[i] = analogRead(sensor);
  }
  insertionsort(x, n);
  return x[n/2];          //Return Median
}
//End of Sensor Reading Functions

/*---------------------------------------------------------------------------------------------------
MDP7                              Recalibrating Robot's Center
---------------------------------------------------------------------------------------------------*/
void frontAlignment(){
  angleAlign();
  delay(100);
}

//MDP7.1
//Angle alignment to ensure robot is facing perpendicular to wall.
void angleAlign(){
  while(1){
    int LFreading = sensorRead(20,LF);
    int RFreading = sensorRead(20,RF);
    int error = LFreading - RFreading;
    
    if(error>3) rotateLeft(1);
    else if (error<-3) rotateRight(1);
      else {
      rotateLeft(1);
      break;
    }
 
  }
  delay(100);
  int LFdistance = distanceInCM(sensorRead(20,LF),LF);
  if(LFdistance > 15  || LFdistance < 15)distAlign();
}

//MDP7.2
//Distance alignment to ensure robot is approx 15cm from wall to front sensors
void distAlign(){
  while(1){
    
    int LFdistance = distanceInCM(sensorRead(20,LF),LF);

    if(LFdistance > 19) md.setSpeeds(150,150);
    else if(LFdistance > 15) md.setSpeeds(80,80);
    else if(LFdistance < 15) md.setSpeeds(-80,-80);
    else break;
  }
   md.setBrakes(400, 400);
   delay(50);
   md.setBrakes(0, 0);
   
   //Recursive call if angle is misaligned after distance alignment.
   int angleError = sensorRead(20,LF) - sensorRead(20,RF);
   if(angleError>3 || angleError<-3) frontAlignment();

}
//End of Robot Alignment Functions

/*---------------------------------------------------------------------------------------------------
MDP8                                  Gradual Left Turn
---------------------------------------------------------------------------------------------------*/
void turnLeft(){
  md.setSpeeds(250,50);
  delay(1225);    //1225 When all fully charged
  md.setBrakes(400,400);
  delay(100);
  md.setBrakes(0,0);
}
//End of turnLeft()

/*---------------------------------------------------------------------------------------------------
MDP9                                 Gradual Right Turn
---------------------------------------------------------------------------------------------------*/
void turnRight(){
  md.setSpeeds(50,250);
  delay(1250);  //1250 When all fully charged
  md.setBrakes(400,400);
  delay(100);
  md.setBrakes(0,0);
}
//End of turnRight()

/*---------------------------------------------------------------------------------------------------
MDP10                                    PID Control
---------------------------------------------------------------------------------------------------*/
int pidControlForward(int left_encoder_val, int right_encoder_val) {
  int error,prevError,pwmL=SPEED,pwmR=SPEED;
  float integral,derivative,output;
  float kp = 1;
  float ki = 1;
  float kd = 1;

  error = right_encoder_val-left_encoder_val;
  integral += error;
  derivative = error - prevError;
  output = kp*error + ki*integral + kd*derivative;
  prevError = error;

  pwmL = output;
  return pwmL;
}
//End of pidControlForward()

//MDP 10.1
int pidControlLR(int left_encoder_val, int right_encoder_val) {
  int error,prevError,pwmL=SPEED,pwmR=SPEED;
  float integral,derivative,output;
  float kp = 1;
  float ki = 1;
  float kd = 1;

  error = right_encoder_val-left_encoder_val;
  integral += error;
  derivative = error - prevError;
  output = kp*error + ki*integral + kd*derivative;
  prevError = error;

  pwmL = output;
  return pwmL;
}
//End of pidControlLR()

/*---------------------------------------------------------------------------------------------------
MDP11                               Insertion Sort Algorithm
---------------------------------------------------------------------------------------------------*/
//Standard insertion sort algorithm
void insertionsort(int array[], int length){
  int i,j;
  int temp;
  for(i = 1; i < length; i++){
    for(j = i; j > 0; j--){
      if(array[j] < array[j-1]){
        temp = array[j];
        array[j] = array[j-1];
        array[j-1] = temp;
      }
      else
        break;
    }
  }
}
//End of Insertion Sort Algorithm

/*---------------------------------------------------------------------------------------------------
                                 Sensor Reading Conversions
---------------------------------------------------------------------------------------------------*/
//MDP12
//This function converts the ADC readings into centimeters
int distanceInCM(int reading, int sensor){
    int cm;

    switch(sensor){
      case LF:
        cm = 6088 / (reading  + 7) - 1;
        break;
      case CF: //Long range
        cm = 6088 / (reading  + 7) - 1;
        break;
      case RF:
        cm = 6088 / (reading  + 7) - 1;
        break;      
      case L:
        cm = 15500.0 / (reading +29) -5;;
        break;      
      case R:
        cm = 6088 / (reading  + 7) - 1;
        break;      
      default:
        return -1;
    }      
   return cm;  
}

//MDP13
//This function converts the cm readings into grids based on sensor type
int distanceInGrids(int dis, int sensorType){
  int grids;
  if(sensorType == SR){ //Short range effective up to 2 grids away
      if (dis > 28) grids =3;
      else if (dis>= 10 && dis <= 19) grids = 1;
      else if (dis >= 20 && dis <= 28) grids = 2;
      //else if (dis >= 32 && dis <= 38) grids = 3;
      else grids = -1;
  }
  else if(sensorType == LR){ //Long range effective up to 5 grids away
      if (dis > 58) grids = 6;
      else if (dis>= 12 && dis <= 22) grids = 1;
      else if (dis > 22 && dis <= 27) grids = 2;
      else if (dis >= 30 && dis <= 37) grids = 3;
      else if (dis >= 39 && dis <= 48) grids = 4;
      else if (dis >= 49 && dis <= 58) grids = 5;
      else grids = -1;
  }
  
  return grids;
}
//End of Sensor Reading Conversion Functions

//MDP 14
void readyMsg(){
  Serial.print("Y\n");
}
//End of readyMsg()

//MDP15
void stopIfFault()
{
  if (md.getM1Fault())
  {
    Serial.println("M1 fault");
    while(1);
  }
  if (md.getM2Fault())
  {
    Serial.println("M2 fault");
    while(1);
  }
}


