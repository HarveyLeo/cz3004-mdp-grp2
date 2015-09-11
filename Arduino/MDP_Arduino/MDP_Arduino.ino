#include "DualVNH5019MotorShield.h"
#include "PinChangeInt.h"

DualVNH5019MotorShield md;

#define motor_R_encoder 5  //Define pins for motor encoder input
#define motor_L_encoder 3

#define MAX_SPEED 400
#define SPEED 200

#define kp 1

int right_encoder_val = 0, left_encoder_val = 0;
void RightEncoderInc(){right_encoder_val++;}
void LeftEncoderInc(){left_encoder_val++;}

void setup()
{
  Serial.begin(115200);
  Serial.println("Dual VNH5019 Motor Shield");
  md.init();
  
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


 while (1){

    if (Serial.available()){
      newChar = Serial.read();
      commands[index] = newChar;
      index++;
      if (newChar == '|'){
        index = 0;
        break;
      }
    }  
  }

  char command = commands[0];
  int grids = 1;
  
  //Hardcode here for testing
  //command = 'A';
  //grids = 1;

  switch ( command ) {
  case 'W':
    {
      moveForward(grids);
      delay(1000);
      break;
    }
    case 'A':
    {
      rotateLeft();
      delay(1000);
      break;
    }
    case 'D':
    {
      rotateRight();
      delay(1000);
      break;
    }
  default:
    {
      break;
    }
    memset(commands,0,sizeof(commands));
  }
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

  int target_Distance = 560 * distance;
  int pwmR = SPEED;
  int pwmL = SPEED;
  int correction=0;
  int counter =0; 
  while(1){
                  if(right_encoder_val > target_Distance){ // break
                    //md.setBrakes(SPEED, SPEED);
                   // delay(0);
                    md.setBrakes(MAX_SPEED, MAX_SPEED);
                    delay(100);
                    md.setBrakes(0, 0);
                    break;
                  }
                  correction =  (right_encoder_val - left_encoder_val)* kp;
                  md.setSpeeds(((correction-pwmR)*-1),pwmL+correction );
                
                 Serial.print(correction);
                 Serial.print(" L: ");  Serial.print(left_encoder_val);
                 Serial.print(" R: ");  Serial.print(right_encoder_val);
                 Serial.print("\n");
                 Serial.print(counter++);
                 Serial.print("\n");
      }
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
