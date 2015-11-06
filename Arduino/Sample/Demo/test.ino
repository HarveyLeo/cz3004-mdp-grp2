//#include "SharpIR.h"
#include "DualVNH5019MotorShield.h"
#include "PinChangeInt.h"
#include "PID_v1.h"

DualVNH5019MotorShield md;

#define LONG 20150 //long
#define SHORT 1080

#define motor1_A        3 // motor 1 white or yellow line
#define motor2_A        5 // motor 2 white or yellowline

// motor control revolution count
#define MAX_SPEED 400
#define COMPEN_RATE 5 // 5x
#define BRAKE_DELAY 100
#define BRAKE_FORCE 300

#define URPWM          13 // PWM Output //pin 4
#define URTRIG          6 // PWM trigger //pin 6

SharpIR sensor1(A0, 20, 98, SHORT);   // (pin , no.reading b4 calculate mean dist, diff btw 2 consecutive measu taken as valid)
SharpIR sensor2(A1, 20, 98, SHORT);
SharpIR sensor3(A2, 35, 98, LONG);
SharpIR sensor4(A3, 35, 98, LONG);

int motor1_encoder = 0, motor2_encoder = 0;
void Motor1EncoderA(){motor1_encoder++ ;}
void Motor2EncoderA(){motor2_encoder++ ;}


void setup(){
  Serial.begin(115200);
  pinMode (A0, INPUT);
  pinMode (A1, INPUT);
  pinMode (A2, INPUT);
  pinMode (A3, INPUT);
  md.init();

  PCintPort::attachInterrupt(motor1_A, Motor1EncoderA, CHANGE);
  PCintPort::attachInterrupt(motor2_A, Motor2EncoderA, CHANGE);
}

void loop(){
  char command[10];
  int index = 0;
while (1){
    if (Serial.available()){
        command[index++] = Serial.read();
            index =0;
            break;
      }
  }

  switch (command[0]) {
      case 'a': case 'A':
      Serial.println('A');
      moveF(1);
      break;
          
     case 'b': case 'B':
     Serial.println('B');
     turnLeft(90);
     break;
          
     case 'c': case 'C':
     Serial.println('C');
     turnRight(90);
     break;
       
    case 'd': case 'D':
    Serial.println('D');
      while(1){
        moveF(1);
        turnRight(90);
      }
     break;
     
     case 'e': case 'E':
    Serial.println('E');
    while(1){
     int dis=sensor1.distance();
     Serial.print("Mean distance: ");  // returns it to the serial monitor
      Serial.println(dis);
    }
    break;

    case 'f': case 'F':
    Serial.println('F');
    while(1)
    {
      moveF(1);
      int sensorvalue = getsensor1();
      if (sensorvalue < 7)
      {
        turnRight(90);
        }       
    }
    break;

    
    }
}
  
 void test(){
  while(1){
  int dis1=sensor1.distance();  // this returns the distance to the object you're measuring  
  Serial.print("Distance1: ");  // returns it to the serial monitor
  Serial.println(dis1);
  
  md.setSpeeds(200,200);
  if (dis1<10)
  {
    md.setSpeeds(-200,-200);
    break;
  }
  }
}

void moveF(int distance){ // ------------------------------------------------------------------------------------------------------moveF
  motor1_encoder=motor2_encoder=0;
  int multiplier;
  switch(distance){
    case 1: multiplier = 550; break;
    default: multiplier = 1000; break;
  }
  int target_Distance = multiplier * distance;
  int pwm1 = 300;
  int pwm2 = pwm1;
  int correction=0;
  int counter =0; 
  while(1){
                  if(motor1_encoder > target_Distance){ // break
                    md.setBrakes(MAX_SPEED, MAX_SPEED);
                    delay(100);
                    md.setBrakes(0, 0);
                    break;
                  }
                  correction =  (motor1_encoder - motor2_encoder)*COMPEN_RATE;
                  md.setSpeeds(((correction-pwm1)*-1), pwm2+correction);
                
                 Serial.print(correction);
                 Serial.print(" L: ");  Serial.print(motor1_encoder);
                 Serial.print(" R: ");  Serial.print(motor2_encoder);
                 Serial.print("\n");
                 Serial.print(counter++);
                 Serial.print("\n");
      }
}


int turnLeft(int angle){// ------------------------------------------------------------------------------------------------------turnLeft
  motor1_encoder= motor2_encoder = 0;
  int correction=0,counter =0, pwm1=400, pwm2=400, target_Angle, breakDelay = BRAKE_DELAY;
  switch(angle){
    case 2: target_Angle = 30; 
            pwm1 = pwm2= 100;
            breakDelay = BRAKE_DELAY;
            break;
    case 90: target_Angle = 720; break;
    default: target_Angle = 1000; break;
  }
  while(1){
                if(motor1_encoder > target_Angle){  
                  md.setBrakes(MAX_SPEED, MAX_SPEED);
                  delay(BRAKE_DELAY);
                  md.setBrakes(0, 0);
                  break;
                 }

                 correction =  (motor1_encoder - motor2_encoder)*COMPEN_RATE;
                 md.setSpeeds(((pwm1-correction)*-1), pwm2+correction); 
                
               Serial.print(correction);
               Serial.print(" LeftPosition: ");   Serial.print(motor1_encoder);
                Serial.print(" RightPosition: ");   Serial.print(motor2_encoder);
               Serial.print("\n");
               Serial.print(counter++);
                Serial.print("\n");
        } 
}
int turnRight(int angle){// ------------------------------------------------------------------------------------------------------turnRight
   motor1_encoder= motor2_encoder = 0;
  int correction=0,counter =0, pwm1=MAX_SPEED, pwm2=MAX_SPEED, target_Angle, breakDelay = BRAKE_DELAY;
  switch(angle){
    case 2: target_Angle = 30; 
            pwm1 = pwm2= 100;
            breakDelay = BRAKE_DELAY;
            break;
    case 90: target_Angle = 720; break;
    default: target_Angle = 1000; break;
  }
  while(1){

                if(motor1_encoder > target_Angle){  
                    md.setBrakes(MAX_SPEED, MAX_SPEED);
                    delay(breakDelay);
                    md.setBrakes(0, 0);
                    break;
                 }
                 
                 correction =  (motor1_encoder - motor2_encoder)*COMPEN_RATE;
                 md.setSpeeds(((correction-pwm1)*-1), -correction-pwm2); 
                
                Serial.print(correction);
               Serial.print(" LeftPosition: ");   Serial.print(motor1_encoder);
                Serial.print(" RightPosition: ");   Serial.print(motor2_encoder);
               Serial.print("\n");
               Serial.print(counter++);
                Serial.print("\n");
        } 
}

int getsensor1()
{

     int dis=sensor1.distance();
      Serial.print("Mean distance: ");  // returns it to the serial monitor
      Serial.println(dis);
      return dis;
}

  
