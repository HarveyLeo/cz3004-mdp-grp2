#include "DualVNH5019MotorShield.h"
#include "PinChangeInt.h"
#include <SharpIR.h>

DualVNH5019MotorShield md;

#define motor_R_encoder 5  //Define pins for motor encoder input
#define motor_L_encoder 3

#define MAX_SPEED 400
#define SPEED 200

#define DIST A2 //DIST: pin where long range sensor is attached to
#define model 20150

int right_encoder_val = 0, left_encoder_val = 0;
void RightEncoderInc(){right_encoder_val++;}
void LeftEncoderInc(){left_encoder_val++;}

SharpIR sharp(DIST,50,90,model);

void setup()
{
  Serial.begin(115200);
  Serial.println("Dual VNH5019 Motor Shield");
  md.init();
  pinMode(DIST,INPUT);
  
  PCintPort::attachInterrupt(motor_R_encoder, RightEncoderInc, CHANGE);
  PCintPort::attachInterrupt(motor_L_encoder, LeftEncoderInc, CHANGE);
}

void loop()
{
   char commands[10];
  int index = 0;
  char newChar;
  left_encoder_val = 0; 
  right_encoder_val = 0; 



// while (1){
//
//    if (Serial.available()){
//      newChar = Serial.read();
//      commands[index] = newChar;
//      index++;
//      if (newChar == '|'){
//        index = 0;
//        break;
//      }
//    }  
//  }

  char command = commands[0];
  int grids = 1;
  
  //Hardcode here for testing
  command = 'T';
  //grids = 1;

  int LF = analogRead(0);
  int L = analogRead(1);
  int F = analogRead(2);
  int R = analogRead(3);
  int RF = analogRead(4);
  
  switch ( command ) {
  case 'W':
    {
      delay(2000);
      moveForward(grids);
      delay(2000);
      break;
    }
    case 'A':
    {
      turnLeft(90);
      delay(1000);
      break;
    }
    case 'D':
    {
      //rotateRight();
      turnRight(90);
      delay(10000);
      break;
    }
    case 'C':
  {
    senseDistance();
    delay(1000);
    break;
  }
    case 'T':
  {
   avoid(LF,L,F,R,RF);
   break;
  }
  default:
    {
      break;
    }
    memset(commands,0,sizeof(commands));
  }
}

void avoid(int LF, int L, int F, int R, int RF){
  if ((LF < 400)&&(RF<400)){
      moveForward(1);
      delay(1000);
  }
  else if ((LF > 400) && (RF>400)){
    if ( L >= 400 ){
      turnRight(90);
      delay(1000);
      }
    else {     
      turnLeft(90);
      delay(1000);
    }
  }
}


int turnRight(int angle){
  left_encoder_val = 0;
  right_encoder_val = 0;
  int pwm1=300, pwm2=300, output=0,LeftPosition,RightPosition;
  int target_Angle;

  if (angle <= 15){
    target_Angle = angle * 8.4;  // 15 BASICALLY OKAY
    pwm1=150;
    pwm2=150;
  }
  else if ((angle > 15) && (angle <= 30))
    target_Angle = angle * 8.4;  // 30 BASICALLY OKAY
  else if ((angle > 30) && (angle <= 45))
    target_Angle = angle * 7.95;


  ///////////////////////
  else if ((angle > 45) && (angle <= 90))  
    target_Angle = angle * 9.2;

  //////////////////////
  else if ((angle > 90) && (angle <= 360))  
    target_Angle = angle * 9.17;
  else if ((angle > 360) && (angle <= 720))  
    target_Angle = angle * 9.0;
  else if ((angle > 720) && (angle <= 1080)) //OKAY
    target_Angle = angle * 9.0;

  while(1){

    LeftPosition =  left_encoder_val;
    RightPosition =  right_encoder_val;  

    //  output = pidControlLeftAndRight(motor1_encoder,motor2_encoder);
   // output = pidControlLeftAndRight(LeftPosition,RightPosition); //hardcoded


    //   md.setSpeeds(pwm1-output, -(pwm2+output));
    md.setSpeeds(-(pwm1-output), pwm2+output);

    if((RightPosition >= target_Angle - 70)&&(angle > 8)){ //used to be left position
      md.setBrakes(400, 400);
      delay(100);
      md.setBrakes(0, 0);
      break;
    }


    if((RightPosition >= target_Angle - 5)&&(angle < 9)){
      md.setBrakes(400, 400);
      delay(100);
      md.setBrakes(0, 0);
      break;
    }
  }

 // if (angle == 90 && getSensor == 1)
  //  exportSensors(); 
}

int turnLeft(int angle){
  left_encoder_val = 0;
  right_encoder_val = 0;
  int pwm1=300, pwm2=300, output=0,LeftPosition,RightPosition;
  int target_Angle;

  if (angle <= 15){
    target_Angle = angle * 8.4;  // 15 BASICALLY OKAY
    pwm1=150;
    pwm2=150;
  }
  else if ((angle > 15) && (angle <= 30))
    target_Angle = angle * 8.4;  // 30 BASICALLY OKAY
  else if ((angle > 30) && (angle <= 45))
    target_Angle = angle * 7.95;


  ///////////////////////
  else if ((angle > 45) && (angle <= 90))  
    target_Angle = angle * 9.2;

  //////////////////////
  else if ((angle > 90) && (angle <= 360))  
    target_Angle = angle * 9.17;
  else if ((angle > 360) && (angle <= 720))  
    target_Angle = angle * 9.22;
  else if ((angle >= 720) && (angle <= 1080)) //OKAY
    target_Angle = angle * 9.2;

  while(1){

    LeftPosition =  left_encoder_val;
    RightPosition =  right_encoder_val;  

    //  output = pidControlLeftAndRight(motor1_encoder,motor2_encoder);
   // output = pidControlLeftAndRight(LeftPosition,RightPosition); //hardcoded


    md.setSpeeds(pwm1-output, -(pwm2+output));
    //md.setSpeeds(-(pwm1-output), pwm2+output);

    if((LeftPosition >= target_Angle - 70)&&(angle > 8)){ //used to be left position
      md.setBrakes(400, 400);
      delay(100);
      md.setBrakes(0, 0);
      break;
    }


    if((LeftPosition >= target_Angle - 5)&&(angle < 9)){
      md.setBrakes(400, 400);
      delay(100);
      md.setBrakes(0, 0);
      break;
    }
  }

 // if (angle == 90 && getSensor == 1)
  //  exportSensors(); 
}

void rotateLeft(){
  int pwmR = SPEED;
  int pwmL = SPEED;
  right_encoder_val = 0;
  while(1){
    left_encoder_val = 0;
    if(right_encoder_val > 765){ // break
    md.setBrakes(MAX_SPEED, MAX_SPEED);
                      delay(100);
                      md.setBrakes(0, 0);
                      break;
                    }
    md.setSpeeds(pwmR,-pwmL);
  }
  
}


void rotateRight(){
  int pwmR = SPEED;
  int pwmL = SPEED;
  left_encoder_val = 0;
  while(1){
    right_encoder_val = 0;
    if(left_encoder_val > 758){ // break
    md.setBrakes(MAX_SPEED, MAX_SPEED);
                      delay(100);
                      md.setBrakes(0, 0);
                      break;
                    }
    md.setSpeeds(-pwmR,pwmL);
  }
  
}

void moveForward(int distance){

  int pwmR = SPEED;
  int pwmL = SPEED;
  int output=0;
  int leftOffset =0; 

  int multiplier;
  switch(distance){
    case 2: multiplier = 585; break;
    case 3: multiplier = 560; break;
    case 4: multiplier = 560; break;
    case 10: multiplier = 600; break;
    case 11: multiplier = 600; break;
    case 12: multiplier = 600; break;
    case 13: multiplier = 600; break;
    case 14: multiplier = 600; break;
    case 15: multiplier = 600; break;
    case 105:{ multiplier = 630; distance = 10;}break;
    case 115:{ multiplier = 630; distance = 11;}break;
    case 125:{ multiplier = 630; distance = 12;}break;
    case 135:{ multiplier = 630; distance = 13;}break;
    case 145:{ multiplier = 630; distance = 14;}break;
    
    default: multiplier = 560; break;
  
  }
  int target_Distance = multiplier * distance;

  //if(  
  while(1){
                  if(right_encoder_val > target_Distance){ // break
                    //md.setBrakes(SPEED, SPEED);
                   // delay(0);
                    md.setBrakes(MAX_SPEED, MAX_SPEED);
                    delay(100);
                    md.setBrakes(0, 0);
                    break;
                  }
                  output = pidControlForward(left_encoder_val,right_encoder_val);
                  md.setSpeeds(pwmR-output+leftOffset,pwmL+output);
      }
}

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

void senseDistance() {
   delay(1000);    // it gives you time to open the serial monitor after you upload the sketch  

  unsigned long pepe1=millis();  // takes the time before the loop on the library begins
  
  int dis=sharp.distance();  // this returns the distance to the object you're measuring

  Serial.print("Mean distance: ");  // returns it to the serial monitor
  Serial.println(dis);
  
  unsigned long pepe2=millis()-pepe1;  // the following gives you the time taken to get the measurement
  Serial.print("Time taken (ms): ");
  Serial.println(pepe2);  

  Serial.print(distanceInGrids(dis));
  Serial.println(" grids away from obstacle!");
}

int distanceInGrids(int dis){
  int grids;
  if (dis>= 7 && dis <= 17)
      grids = 1;
  else if (dis >= 18 && dis <= 29)
      grids = 2;
  else if (dis >= 30 && dis <= 42)
      grids = 3;
  else if (dis >= 43 && dis <= 60)
      grids = 4;
  else
      grids = 0;

  return grids;
  
}

//void centreRecalibrate(){
//  int pwmL = SPEED;
//  int pwmR = SPEED;
//
//  
//}

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
