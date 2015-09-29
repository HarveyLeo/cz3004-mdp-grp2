#include "DualVNH5019MotorShield.h"
#include "PinChangeInt.h"

DualVNH5019MotorShield md;

#define motor_R_encoder 5  //Define pins for motor encoder input
#define motor_L_encoder 3

#define MAX_SPEED 400
#define SPEED 200

#define LF A0   //Left-front
#define L  A5   //Left
#define CF  A2  //Centre-front
#define R  A3   //Right
#define RF A4   //Right-front
#define SR 0    //Short-range sensor
#define LR 1    //Long-range sensor

int right_encoder_val = 0, left_encoder_val = 0;
void RightEncoderInc(){right_encoder_val++;}
void LeftEncoderInc(){left_encoder_val++;}

void setup()
{
  Serial.begin(115200);
  md.init();
  PCintPort::attachInterrupt(motor_R_encoder, RightEncoderInc, CHANGE);
  PCintPort::attachInterrupt(motor_L_encoder, LeftEncoderInc, CHANGE);
}

void loop()
{
  char command_buffer[10];
  int i = 0, arg = 0, digit = 1;
  char newChar;
  left_encoder_val = 0; 
  right_encoder_val = 0; 

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
 // Serial.println(arg);
 
  //Hardcode here for testing
 // command = 'L';
 // arg = 90;
    
  switch ( command ) {
  case 'W':
    {
      delay(1000);
      if(arg == 0) moveForward(1);
      else moveForward(arg);
      sendSensors();
      break;
    }
    case 'A':
    {
      delay(1000);
      if(arg == 0) rotateLeft(90);
      else rotateLeft(arg);
      sendSensors();
      break;
    }
    case 'D':
    {
      delay(1000);
      if(arg == 0) rotateRight(90);
      else rotateRight(arg);
      sendSensors();
      break;
    }
    case 'E':
   {
      delay(1000);
      sendSensors();
      break;
   }
   case 'C':
   {
      frontAlignment();
      sendSensors();
      break;
   }
     case 'T':
   {
     delay(3000);
     avoid();
     break;
   }
     case 'L':
     {
      turnLeft();
      sendSensors();
      break;
     }
     case 'R':
     {
      turnRight();
      sendSensors();
      break;
     }
    default:
   {
     break;
   }
    memset(command_buffer,0,sizeof(command_buffer));
  }
}

//This function is avoid any 1x1 obstacle placed in the path of the robot
void avoid(){
  int CFdistance, RFdistance, LFdistance, Ldistance, Rdistance;
 
  while(1){
    moveForward(1);
    
    int LFreading = sensorRead(20,LF);
    int CFreading = sensorRead(60,CF);
    int RFreading = sensorRead(20,RF);
    int Lreading = sensorRead(20,L);
    int Rreading = sensorRead(20,R);

    Serial.print("LF:");
    Serial.println(LFreading);
    LFdistance = 6088 / (LFreading  + 7) - 1;
    Serial.println(LFdistance);
    Serial.println("CF:");
    Serial.println(CFreading);
    CFdistance = 15500.0 / (CFreading +29) -5;
    Serial.println(CFdistance);
    Serial.print("RF:");
    Serial.println(RFreading);
    RFdistance = 6088 / (RFreading  + 7) - 1;
    Serial.println(RFdistance);
    Serial.print("L:");
    Serial.println(Lreading);
    Ldistance = 6088 / (Lreading  + 7) - 1;
    Serial.println(Ldistance);
    Serial.print("R:");
    Serial.println(Rreading);
    Rdistance = 6088 / (Rreading  + 7) - 1;
    Serial.println(Rdistance);
    
    if (RFreading >= 275 && RFreading <= 435){ //Obstacle at RF sensor
      Serial.println("RF Detected");
      delay(1000);
      rotateRight(90);
      delay(1000);
      moveForward(3);
      delay(1000);
      rotateLeft(90);
      delay(1000);
      moveForward(4);
      delay(1000);
      rotateLeft(90);
      delay(1000);
      moveForward(3);
      delay(1000);
      rotateRight(90);
    }
    else if(CFdistance >=20 && CFdistance <= 23){ //Obstacle at CF sensor
      Serial.println("CF Detected");
      delay(1000);
      rotateRight(90);
      delay(1000);
      moveForward(2);
      delay(1000);
      rotateLeft(90);
      delay(1000);
      moveForward(4);
      delay(1000);
      rotateLeft(90);
      delay(1000);
      moveForward(2);
      delay(1000);
      rotateRight(90);
    }
    else if( LFreading >= 275 && LFreading <= 435){ //Obstacle at LF sensor
      Serial.println("LF Detected");
      delay(1000);
      rotateRight(90);
      delay(1000);
      moveForward(1);
      delay(1000);
      rotateLeft(90);
      delay(1000);
      moveForward(4);
      delay(1000);
      rotateLeft(90);
      delay(1000);
      moveForward(1);
      delay(1000);
      rotateRight(90);
      
    }
    
    delay(1000);
  }
}


int rotateRight(int angle){
  left_encoder_val = 0;
  right_encoder_val = 0;
  int pwm1=300, pwm2=300, output=0;
  int angle_offset;

  if (angle <= 15){
    angle_offset = angle * 8.4;  
    pwm1=150;
    pwm2=150;
  }
  else if ((angle > 15) && (angle <= 30))
    angle_offset = angle * 8.4;  
  else if ((angle > 30) && (angle <= 45))
    angle_offset = angle * 8.95;

  else if ((angle > 45) && (angle <= 90)) //Only this one calibrated 
    angle_offset = angle * 8.95;

  else if ((angle > 90) && (angle <= 360))  
    angle_offset = angle * 8.95;
  else if ((angle > 360) && (angle <= 720))  
    angle_offset = angle * 8.95;
  else if ((angle > 720) && (angle <= 1080)) 
    angle_offset = angle * 8.95;

  while(1){
        
    md.setSpeeds(-(pwm1-output), pwm2+output);

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
  }
}

int rotateLeft(int angle){
  left_encoder_val = 0;
  right_encoder_val = 0;
  int pwm1=300, pwm2=300, output=0;
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
    
  else if ((angle > 45) && (angle <= 90))   //Only this one calibrated 
    angle_offset = angle * 8.9;

  else if ((angle > 90) && (angle <= 360))  
    angle_offset = angle * 8.9;
  else if ((angle > 360) && (angle <= 720))  
    angle_offset = angle * 8.9;
  else if ((angle > 720) && (angle <= 1080))
    angle_offset = angle * 8.9;

  while(1){

    md.setSpeeds(pwm1-output, -(pwm2+output));

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
  }
}

//Function for making a left turn on a 2x2 grid.
void turnRight(){
  md.setSpeeds(50,250);
  delay(1250);
  md.setBrakes(400,400);
  delay(100);
  md.setBrakes(0,0);
}

//Function for making a right turn on a 2x2 grid.
void turnLeft(){
  md.setSpeeds(250,50);
  delay(1250);
  md.setBrakes(400,400);
  delay(100);
  md.setBrakes(0,0);
}

void moveForward(int distance){
  
  left_encoder_val = 0; 
  right_encoder_val = 0; 
  int pwmR = SPEED;
  int pwmL = SPEED;
  int output=0;

  int multiplier;
  switch(distance){
    case 2: multiplier = 580; break;
    case 3: multiplier = 585; break;
    case 4: multiplier = 590; break;
    case 5: multiplier = 590; break;
    case 6: multiplier = 590; break;
    case 7: multiplier = 590; break;
    case 8: multiplier = 590; break;
    case 9: multiplier = 590; break;
    case 10: multiplier = 600; break;
    case 11: multiplier = 600; break;
    case 12: multiplier = 600; break;
    case 13: multiplier = 600; break;
    case 14: multiplier = 600; break;
    case 15: multiplier = 600; break;
    default: multiplier = 560; break;
  
  }
  int target_Distance = multiplier * distance;
  
  while(1){
                  if(right_encoder_val > target_Distance){ // break
                    md.setBrakes(MAX_SPEED, MAX_SPEED-20);
                    delay(100);
                    md.setBrakes(0, 0);
                    break;
                  }
                  output = pidControlForward(left_encoder_val,right_encoder_val);
                  md.setSpeeds(pwmR-output,pwmL+output);
      }
      
}

//This function is used to implement the PID control
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

//Function to align robot to front wall
void frontAlignment(){
  angleAlign();
  delay(100);
}

//Angle alignment to ensure robot is facing perpendicular to wall.
void angleAlign(){
  while(1){
    int LFreading = sensorRead(20,LF);
    int RFreading = sensorRead(20,RF);
    int error = LFreading - RFreading;
    
    if(error>2) rotateLeft(1);
    else if (error<-2) rotateRight(1);
    else break;
  }
  delay(100);
  distAlign();
}

//Distance alignment to ensure robot is approx 15cm from wall to front sensors
void distAlign(){
  while(1){
    int LFdistance = distanceInCM(sensorRead(20,LF),LF);

    if(LFdistance > 15) md.setSpeeds(100,100);
    else if(LFdistance < 15) md.setSpeeds(-100,-100);
    else break;
  }
   md.setBrakes(400, 400);
   delay(50);
   md.setBrakes(0, 0);
   
   //Recursive call if angle is misaligned after distance alignment.
   int angleError = sensorRead(20,LF) - sensorRead(20,RF);
   if(angleError>5 || angleError<-5) frontAlignment();

}
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

//This function sends the sensor data to the RPi
void sendSensors(){
  int cmLF, cmCF, cmRF, cmL, cmR;
  cmLF = distanceInCM(sensorRead(20,LF),LF);
  cmCF = distanceInCM(sensorRead(60,CF),CF);
  cmRF = distanceInCM(sensorRead(20,RF),RF);
  cmL = distanceInCM(sensorRead(20,L),L);
  cmR = distanceInCM(sensorRead(20,R),R);
  
  
  Serial.print(":");
  Serial.print(distanceInGrids(cmLF,SR));
  Serial.print(":");
  //Serial.println(cmLF);
  Serial.print(distanceInGrids(cmCF,LR));
  Serial.print(":");
  //Serial.println(cmCF);
  Serial.print(distanceInGrids(cmRF,SR));
  Serial.print(":");
  //Serial.println(cmRF);
  Serial.print(distanceInGrids(cmL,SR));
  Serial.print(":");
  //Serial.println(cmL);
  Serial.print(distanceInGrids(cmR,SR));
  //Serial.println(cmR);
  Serial.print("|");
}

//This function converts the ADC readings into centimeters
int distanceInCM(int reading, int sensor){
    int cm;

    switch(sensor){
      case LF:
        cm = 6088 / (reading  + 7) - 1;
        break;
      case CF: //Long range
        cm = 15500.0 / (reading +29) -5;
        break;
      case RF:
        cm = 6088 / (reading  + 7) - 1;
        break;      
      case L:
        cm = 6088 / (reading  + 7) - 1;
        break;      
      case R:
        cm = 6088 / (reading  + 7) - 1;
        break;      
      default:
        return -1;
    }      
   return cm;  
}

//This function converts the cm readings into grids based on sensor type
int distanceInGrids(int dis, int sensorType){
  int grids;
  
  if(sensorType == SR){ //Short range effective up to 2 grids away
      if (dis > 28) grids = 0;
      else if (dis>= 10 && dis <= 19) grids = 1;
      else if (dis >= 20 && dis <= 28) grids = 2;
      //else if (dis >= 32 && dis <= 38) grids = 3;
      else grids = -1;
  }
  else if(sensorType == LR){ //Long range effective up to 5 grids away
      if (dis > 58) grids = 0;
      else if (dis>= 12 && dis <= 23) grids = 1;
      else if (dis > 24 && dis <= 27) grids = 2;
      else if (dis >= 30 && dis <= 37) grids = 3;
      else if (dis >= 39 && dis <= 48) grids = 4;
      else if (dis >= 49 && dis <= 58) grids = 5;
      else grids = -1;
  }
  
  return grids;
}

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

//void senseDistance() {
//   delay(1000);    // it gives you time to open the serial monitor after you upload the sketch  
//
//  unsigned long pepe1=millis();  // takes the time before the loop on the library begins
//  
//  int dis=sharp.distance();  // this returns the distance to the object you're measuring
//
//  Serial.print("Mean distance: ");  // returns it to the serial monitor
//  Serial.println(dis);
//  
//  unsigned long pepe2=millis()-pepe1;  // the following gives you the time taken to get the measurement
//  Serial.print("Time taken (ms): ");
//  Serial.println(pepe2);  
//
//  Serial.print(distanceInGrids(dis));
//  Serial.println(" grids away from obstacle!");
//}

