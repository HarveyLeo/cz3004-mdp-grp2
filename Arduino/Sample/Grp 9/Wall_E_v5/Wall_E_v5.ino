/*

CE/CZ3004 Multidisciplinary Design Project - Wall_E_v5

Motor 1 - Right, looking from the back, Lebot facing forward - Slower, Lesser Ticks
Motor 2 - Left, looking from the back, Lebot facing forward - Faster, More Ticks

Robot`s Mapping

       [123]
    [0] XXX [4]
        XXX  0
    [X] XXX [6]
 
 Mapping returns an array which a integer value (0-1) representing the number of grids the obstacle is away from the robot.
 
 */
#include "PinChangeInt.h"
#define arraySize 10

//----------------------Sensors------------------------------------------------------------
int frontRightSensorPin = A0;    // select the input pin for the potentiometer
int frontLeftSensorPin = A3;

int frontCentreSensorPin = A4;

int rightFrontSensorPin = A1;
int rightBackSensorPin = A2;

int leftSensorPin = A5;

double leftSensorArray[arraySize], rightFrontSensorArray[arraySize], rightBackSensorArray[arraySize], frontRightSensorArray[arraySize], frontLeftSensorArray[arraySize];
double frontCentreSensorArray[arraySize];
double leftSensorMedian=0.0, rightFrontSensorMedian=0.0, rightBackSensorMedian=0.0, frontRightSensorMedian=0.0, frontLeftSensorMedian=0.0;
double frontCentreSensorMedian=0.0;

double calibratedFrontCentreDist = 9.4;

//-------------------Initialisation--------------------------------------------------------
//M1 direction inputs
int m1INA = 2;
int m1INB = 4;

//M2 direction inputs
int m2INA = 7;
int m2INB = 8;

//M1 & M2 PWM speed input
int m1PWM = 9;
int m2PWM = 10;

int command = 0;
static int lastCommand = 0;

//----------------Motor movement---------------------------------------------------------
//Measuring movement counts and ticks of m1 and m2
volatile int m1Ticks=0, m2Ticks=0;
volatile int m1MovementCount=0, m2MovementCount=0, avgCount=0;

double m1DC = 0, m2DC = 0; //Remember to set new values in each case
double kP = 0.0, kD = 0.0, kI = 0.0; //Remember to set new values in each case

unsigned long prev_ms = 0;
unsigned long interval = 10;
unsigned long sensorDelayTime = 400;

//PID Parameters
double curError = 0.0;
double prevError = 0.0;
double sumError = 0.0;
double dError = 0.0;

double m1Speed = 0.0;
double m2Speed = 0.0;

//----------------Arena Mapping---------------------------------------------------------
int objPos[7];
boolean sendMapping=false, explorationMode=false, startFlag=false;

void setup() {  
  Serial.begin(115200);
  
  //Motor 1 (Right)
  pinMode(m1INA, OUTPUT); //Motor 1 direction input A
  pinMode(m1INB, OUTPUT); //Motor 1 direction input B  
  pinMode(m1PWM, OUTPUT); //Motor 1 speed input

  //Motor 2 (Left)
  pinMode(m2INA, OUTPUT); //Motor 2 direction input A
  pinMode(m2INB, OUTPUT); //Motor 2 direction input B  
  pinMode(m2PWM, OUTPUT); //Motor 2 speed input
  
  //Interrupt driven pin to count motor ticks and movement counts
  pinMode(PIN3, INPUT); //Interrupt Pin 3 - M1 (Right)
  pinMode(PIN5, INPUT); //Interrupt Pin 5 - M2 (Left)
  
  // attach a PinChange Interrupt to our pin on the rising edge
  // (RISING, FALLING and CHANGE all work with this library)
  PCintPort::attachInterrupt(PIN3, &compute_m1_ticks,RISING); //Attached to Pin 3
  PCintPort::attachInterrupt(PIN5, &compute_m2_ticks,RISING); //Attached to Pin 5
  
  //Poll Serial for Command - Establishing Communications
  while(!Serial);
  
  while(!startFlag){
    while(Serial.available()){
      char inChar = (char)Serial.read();
      if(inChar == 'h'){
        explorationMode = true;
        sendMapping = true;
        startFlag = true;
      }
    }
  }
}

void loop() {
  switch(command){
    case 0:
      if(sendMapping){
        objScan();
        sendMapping = false;
        
        // if obstacle position is x1x1x, reposition robot
        if(objPos[1]==1 && objPos[3]==1){
          repositionRobotFront();
          if(objPos[2]==1){
            //if robot is too close to the wall, back away from the wall
            realignRobotCentre();
              
            //objScan();
            repositionRobotFront();
          }
        }
        else if(objPos[1]==1 && objPos[2]==1){
          //repositionRobotFrontLeft();
          realignRobotCentre();
          repositionRobotFrontLeft();
        }
        else if(objPos[2]==1 && objPos[3]==1){
          //repositionRobotFrontRight();
          realignRobotCentre();
          repositionRobotFrontRight();
        }
//        else if(objPos[2]==1){
//          //if robot is too close to the wall, back away from the wall
//          realignRobotCentre();
//        }
        // if right side of the robot is next to a wall
        if(objPos[4]==1 && objPos[6]==1){
          repositionRobotRightSide();
        }
      }
      //objScan();
          
      //command = 1;
      //command = 50;
      //repositionRobotFront();
      //repositionRobotFrontLeft();
      //repositionRobotFrontRight();
      //realignRobotCentre();
      //repositionRobotRightSide();
      //command = 50;
      //delay(10000);
    break;
    
    case 1: //Moving 1 Grid
      oneGridForward();
      
      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;
    
    case 2: //Rotate Right 90   
      rotateRight90();
      
      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;
    
    case 3: //Rotate Left 90
      rotateLeft90();
      
      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;
    
    case 4: //Rotate Right 180
      //rotateRight180();
      
      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;
    
    case 5: //Reposition Robot Front
      repositionRobotFront();
      //realignRobotCentre();
      //for(long startTime=millis();(millis()-startTime)<1000;){
      realignRobotCentre();
      //}
      //delay(300);
      repositionRobotFront();
      
      lastCommand = command;
      command = 0;
      sendMapping = true; 
    break;
    
    case 6: //Parking for Fastest Path Run
      //realignRobotCentreParking();
      
      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;
    
    case 7: //Turn Right Reposition and Realign Robot Front Centre, Turn Back Reposition Right (Robot Position Stay the Same)
      rotateRight90();
      delay(300);
      repositionRobotFront();

      //for(long startTime=millis();(millis()-startTime)<1000;){
      realignRobotCentre();
      //}
      //delay(300);
      repositionRobotFront();
      //delay(300);
      rotateLeft90();
      delay(300);
      repositionRobotRightSide();

      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;

    case 8:
//      rotateLeft90();
//      repositionRobotFront();
//      realignRobotCentre();
      rotateLeft90();
      delay(300); //1000
      repositionRobotFront();
//      realignRobotCentre();
//      delay(1000);

      //for(long startTime=millis();(millis()-startTime)<1000;){
      realignRobotCentre();
      //}
      //delay(300);
      repositionRobotFront();
      //delay(300);
      rotateRight90();
//      delay(500); //1000
//      repositionRobotFront();
      
      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;
    
    case 9:
//      repositionRobotFront();
//      //realignRobotCentre2();
//      repositionRobotFront();
      rotateRight90();
      delay(300);
      repositionRobotFront();

      realignRobotCentre();
      //delay(300);
      repositionRobotFront();
      //delay(300);
      rotateLeft90();

      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;
    
    case 49: //Moving 2 grids - 'a'
      // Setting wheels to move robot forward
      digitalWrite(m1INA, HIGH);
      digitalWrite(m1INB, LOW);
    
      digitalWrite(m2INA, HIGH);
      digitalWrite(m2INB, LOW);
      
      //Set Duty Cycle
      m1DC = 405; //Right //195
      m2DC = 400;         //190
      
      //Set PID Parameters
      kP = 0;
      kD = 0;
      kI = 0;
      
      for(m2MovementCount=0, m1MovementCount=0, m1Ticks=0, m2Ticks=0, avgCount=0; avgCount<490;){
        moveForward();
        avgCount = (m2MovementCount+m1MovementCount)/2;
      }
     
      prevError = 0;
      
      //brake();
      //LOW brake  
      digitalWrite(m1INA, LOW);
      digitalWrite(m1INB, LOW);
      
      digitalWrite(m2INA, LOW);
      digitalWrite(m2INB, LOW);
     
      analogWrite(m1PWM, 0.86*255);
      analogWrite(m2PWM, 0.95*255);
      
      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;
    
    case 50: //Moving forward 3 Grids - 'b'
      // Setting wheels to move robot forward
      digitalWrite(m1INA, HIGH);
      digitalWrite(m1INB, LOW);
    
      digitalWrite(m2INA, HIGH);
      digitalWrite(m2INB, LOW);
      
      //Set Duty Cycle
//      m1DC = 405; //Right //195
//      m2DC = 398;         //190
      
      m1DC = 990; //Right //850
      m2DC = 890;
      
      //Set PID Parameters
      kP = 0;
      kD = 0;
      kI = 0;
      
      for(m2MovementCount=0, m1MovementCount=0, m1Ticks=0, m2Ticks=0, avgCount=0; avgCount<740;){
        moveForward();
        avgCount = (m2MovementCount+m1MovementCount)/2;
      }
     
      prevError = 0;
      
      //brake();
      //LOW brake  
      digitalWrite(m1INA, LOW);
      digitalWrite(m1INB, LOW);
      
      digitalWrite(m2INA, LOW);
      digitalWrite(m2INB, LOW);
     
      analogWrite(m1PWM, 0.9*255); //0.86
      analogWrite(m2PWM, 0.95*255);
      
      delay(300);
      
      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;
    
    case 51: // - 'c'
      // Setting wheels to move robot forward
      digitalWrite(m1INA, HIGH);
      digitalWrite(m1INB, LOW);
    
      digitalWrite(m2INA, HIGH);
      digitalWrite(m2INB, LOW);
      
      //Set Duty Cycle
      m1DC = 405; //Right //195
      m2DC = 400;         //190
      
      //Set PID Parameters
      kP = 0;
      kD = 0;
      kI = 0;
      
      for(m2MovementCount=0, m1MovementCount=0, m1Ticks=0, m2Ticks=0, avgCount=0; avgCount<2480;){
        moveForward();
        avgCount = (m2MovementCount+m1MovementCount)/2;
      }
     
      prevError = 0;
      
      //brake();
      //LOW brake  
      digitalWrite(m1INA, LOW);
      digitalWrite(m1INB, LOW);
      
      digitalWrite(m2INA, LOW);
      digitalWrite(m2INB, LOW);
     
      analogWrite(m1PWM, 0.86*255);
      analogWrite(m2PWM, 0.95*255);
    
      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;
    
    case 52: // - 'd'
      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;
    
    case 53:      
      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;
    
    case 54: //Test Sensors
      Serial.print(frontLeftSensorMedian);
      Serial.print("  ");
      Serial.print(frontCentreSensorMedian);
      Serial.print("  ");
      Serial.print(frontRightSensorMedian);
      Serial.print("\n");
      Serial.print(leftSensorMedian);
      Serial.print("        ");
      Serial.print(rightFrontSensorMedian);
      Serial.print("\n");
      Serial.print("            ");
      Serial.print(rightBackSensorMedian);     
      Serial.print("\n\n");
      
      lastCommand = command;
      command = 0;
      sendMapping = true;
    break;
    
    default:
      command = 0;
    break;
  }  
}

void serialEvent(){  
  if (Serial.available() > 0){
    char inChar = (char)Serial.read();
    if (inChar == 'f'){ //f indicate start of the fastest path run
      //explorationMode = false;
      sensorDelayTime = 100;
    }
    else if (inChar != 'f'){
      //Read the incoming byte: [.read()- char '0' = .read() - 48]
      //command = Serial.read() - '0';
      command = inChar - '0';
      //sendMapping = true;
    }
  }
  else{
    command = 0;
  }
}

void objScan(){
  for(long startTime=millis();(millis()-startTime)<sensorDelayTime;)
  {
    computeMedian();
  }
  
  //---------------Scan Left----------------------------------------     
  //Scan Left
  if(leftSensorMedian<=24 && leftSensorMedian>0)
    objPos[0] = 1;
  else 
    objPos[0] = 0;

  //---------------Scan Front----------------------------------------    
  //Scan Front Left
  if(frontLeftSensorMedian<=14 && frontLeftSensorMedian>0)
    objPos[1] = 1;
  else 
    objPos[1] = 0;

  if(frontCentreSensorMedian<=14 && frontCentreSensorMedian>0)
    objPos[2] = 1;
  else 
    objPos[2] = 0;
    
  //Scan Front Right
  if(frontRightSensorMedian<=14 && frontRightSensorMedian>0)
    objPos[3] = 1;
  else
    objPos[3] = 0;

  //---------------Scan Right----------------------------------------     
  //Scan Right Front
  if(rightFrontSensorMedian<=17 && rightFrontSensorMedian>0)
    objPos[4] = 1;
  else 
    objPos[4] = 0;
    
  //Scan Right Back
  if(rightBackSensorMedian<=17 && rightBackSensorMedian>0)
    objPos[6] = 1;
  else 
    objPos[6] = 0;
    
  if((sendMapping&&explorationMode)/*||(!explorationMode&&(commandBuffer.length()==0))*/){
    Serial.print(objPos[0]);
    Serial.print(objPos[1]);
    Serial.print(objPos[2]);
    Serial.print(objPos[3]);
    Serial.print(objPos[4]);
    Serial.print(0);
    Serial.print(objPos[6]);
    Serial.print("\n");
    //Serial.print(msgCount);
    //Serial.println(lastCommand);
  }
}

void computeMedian(){
  static int index=0;

  // get reading
  leftSensorArray[index] = leftSensorReading();
  rightFrontSensorArray[index] = rightFrontSensorReading();
  rightBackSensorArray[index] = rightBackSensorReading();
  frontRightSensorArray[index] = frontRightSensorReading();
  frontLeftSensorArray[index] = frontLeftSensorReading();
  frontCentreSensorArray[index] = frontCentreSensorReading();
  
  // sort data     
  insertionSort();
  
  leftSensorMedian = leftSensorArray[arraySize/2];
  rightFrontSensorMedian = rightFrontSensorArray[arraySize/2];
  rightBackSensorMedian = rightBackSensorArray[arraySize/2];
  frontRightSensorMedian = frontRightSensorArray[arraySize/2];
  frontLeftSensorMedian = frontLeftSensorArray[arraySize/2];
  frontCentreSensorMedian = frontCentreSensorArray[arraySize/2];
  
  index = (index+1)%arraySize;
}

void insertionSort(){
  for(int i=1; i<arraySize; i++){
    for(int j=i; j>0;j--){
      if(leftSensorArray[j]<leftSensorArray[j-1]){
        double temp = leftSensorArray[j];
        leftSensorArray[j] = leftSensorArray[j-1];
        leftSensorArray[j-1] = temp;
      }
      if(rightFrontSensorArray[j]<rightFrontSensorArray[j-1]){
        double temp = rightFrontSensorArray[j];
        rightFrontSensorArray[j] = rightFrontSensorArray[j-1];
        rightFrontSensorArray[j-1] = temp;
      }
      if(rightBackSensorArray[j]<rightBackSensorArray[j-1]){
        double temp = rightBackSensorArray[j];
        rightBackSensorArray[j] = rightBackSensorArray[j-1];
        rightBackSensorArray[j-1] = temp;
      }
      if(frontRightSensorArray[j]<frontRightSensorArray[j-1]){
        double temp = frontRightSensorArray[j];
        frontRightSensorArray[j] = frontRightSensorArray[j-1];
        frontRightSensorArray[j-1] = temp;
      }
      if(frontLeftSensorArray[j]<frontLeftSensorArray[j-1]){
        double temp = frontLeftSensorArray[j];
        frontLeftSensorArray[j] = frontLeftSensorArray[j-1];
        frontLeftSensorArray[j-1] = temp;
      }
      if(frontCentreSensorArray[j]<frontCentreSensorArray[j-1]){
        double temp = frontCentreSensorArray[j];
        frontCentreSensorArray[j] = frontCentreSensorArray[j-1];
        frontCentreSensorArray[j-1] = temp;
      }
    }
  } 
}

void computeFrontRepositionMedian(){
  static int index=0;

  // get reading
  frontRightSensorArray[index] = frontRightSensorReading();
  frontLeftSensorArray[index] = frontLeftSensorReading();
  frontCentreSensorArray[index] = frontCentreSensorReading();
  
  // sort data     
  frontRepositionInsertionSort();
  
  frontRightSensorMedian = frontRightSensorArray[arraySize/2];
  frontLeftSensorMedian = frontLeftSensorArray[arraySize/2];
  frontCentreSensorMedian = frontCentreSensorArray[arraySize/2];
  
  index = (index+1)%arraySize;
}

void frontRepositionInsertionSort(){
  for(int i=1; i<arraySize; i++){
    for(int j=i; j>0;j--){
      if(frontRightSensorArray[j]<frontRightSensorArray[j-1]){
        double temp = frontRightSensorArray[j];
        frontRightSensorArray[j] = frontRightSensorArray[j-1];
        frontRightSensorArray[j-1] = temp;
      }
      if(frontLeftSensorArray[j]<frontLeftSensorArray[j-1]){
        double temp = frontLeftSensorArray[j];
        frontLeftSensorArray[j] = frontLeftSensorArray[j-1];
        frontLeftSensorArray[j-1] = temp;
      }
      if(frontCentreSensorArray[j]<frontCentreSensorArray[j-1]){
        double temp = frontCentreSensorArray[j];
        frontCentreSensorArray[j] = frontCentreSensorArray[j-1];
        frontCentreSensorArray[j-1] = temp;
      }
    }
  } 
}

void repositionRobotFront(){
  int reposCount=0;
  //while(frontLeftSensorMedian!=frontRightSensorMedian && reposCount<15){
  while(abs(frontLeftSensorMedian - frontRightSensorMedian) > 0.4 && reposCount<15){
    m2MovementCount=0; 
    m1MovementCount=0; 
    avgCount=0; 
    if(frontLeftSensorMedian>frontRightSensorMedian){  
      // set wheels to rotate right
      digitalWrite(m1INA, LOW);
      digitalWrite(m1INB, HIGH);
      digitalWrite(m2INA, HIGH);
      digitalWrite(m2INB, LOW);
    } else if(frontLeftSensorMedian<frontRightSensorMedian){
      // set wheels to rotate left
      digitalWrite(m1INA, HIGH);
      digitalWrite(m1INB, LOW);
      digitalWrite(m2INA, LOW);
      digitalWrite(m2INB, HIGH);
    }
    // start rotating
    analogWrite(m2PWM, 0.1*255);
    analogWrite(m1PWM, 0.13*255);
    while(avgCount<5){
      avgCount = (m2MovementCount+m1MovementCount)/2;
    }
    // Setting wheels to brake
    digitalWrite(m1INB, LOW);
    digitalWrite(m2INB, LOW);
    digitalWrite(m1INA, LOW);
    digitalWrite(m2INA, LOW);
    analogWrite(m2PWM, 255);
    analogWrite(m1PWM, 255);
    for(long startTime=millis();(millis()-startTime)<50;){
      //computeMedian();
      computeFrontRepositionMedian();
    }
    reposCount++;
  }
}

void repositionRobotFrontRight(){
  int reposCount=0;
  //while(frontCentreSensorMedian!=frontRightSensorMedian && reposCount<15){
  while(abs(frontCentreSensorMedian - frontRightSensorMedian) > 0.1 && reposCount<15){
    m2MovementCount=0; 
    m1MovementCount=0; 
    avgCount=0; 
    if(frontCentreSensorMedian>frontRightSensorMedian){  
      // set wheels to rotate right
      digitalWrite(m1INA, LOW);
      digitalWrite(m1INB, HIGH);
      digitalWrite(m2INA, HIGH);
      digitalWrite(m2INB, LOW);
    } else if(frontCentreSensorMedian<frontRightSensorMedian){
      // set wheels to rotate left
      digitalWrite(m1INA, HIGH);
      digitalWrite(m1INB, LOW);
      digitalWrite(m2INA, LOW);
      digitalWrite(m2INB, HIGH);
    }
    // start rotating
    analogWrite(m1PWM, 0.13*255);
    analogWrite(m2PWM, 0.1*255);
    while(avgCount<5){
      avgCount = (m2MovementCount+m1MovementCount)/2;
    }
    // Setting wheels to brake
    digitalWrite(m1INB, LOW);
    digitalWrite(m2INB, LOW);
    digitalWrite(m1INA, LOW);
    digitalWrite(m2INA, LOW);
    analogWrite(m2PWM, 255);
    analogWrite(m1PWM, 255);
    for(long startTime=millis();(millis()-startTime)<50;){
      //computeMedian();
      computeFrontRepositionMedian();
    }
    reposCount++;
  }
}

void repositionRobotFrontLeft(){
  int reposCount=0;
  //while(frontLeftSensorMedian!=frontCentreSensorMedian && reposCount<15){
  while(abs(frontLeftSensorMedian - frontCentreSensorMedian) > 0.4 && reposCount<15){
    m2MovementCount=0; 
    m1MovementCount=0; 
    avgCount=0;
    if(frontLeftSensorMedian>frontCentreSensorMedian){  
      // set wheels to rotate right
      digitalWrite(m1INA, LOW);
      digitalWrite(m1INB, HIGH);
      digitalWrite(m2INA, HIGH);
      digitalWrite(m2INB, LOW);
    } else if(frontLeftSensorMedian<frontCentreSensorMedian){
      // set wheels to rotate left
      digitalWrite(m1INA, HIGH);
      digitalWrite(m1INB, LOW);
      digitalWrite(m2INA, LOW);
      digitalWrite(m2INB, HIGH);
    }
    // start rotating
    analogWrite(m2PWM, 0.1*255);
    analogWrite(m1PWM, 0.13*255);
    while(avgCount<5){
      avgCount = (m2MovementCount+m1MovementCount)/2;
    }
    // Setting wheels to brake
    digitalWrite(m1INB, LOW);
    digitalWrite(m2INB, LOW);
    digitalWrite(m1INA, LOW);
    digitalWrite(m2INA, LOW);
    analogWrite(m2PWM, 255);
    analogWrite(m1PWM, 255);
    for(long startTime=millis();(millis()-startTime)<50;){
      //computeMedian();
      computeFrontRepositionMedian();
    }
    reposCount++;
  }
}

void realignRobotCentre(){  // might need to use IR instead of UR if too close to the wall unless we calibrate to never move more then 1 grid
  int reposCount=0;
  // get front sonic dist
  //sonicReading();
  //if(sonicDist!=6/* && explorationMode*/){
  //if(frontCentreSensorMedian!=9){
  if(abs(frontCentreSensorMedian - calibratedFrontCentreDist) > 0.1){
    //while(frontCentreSensorMedian!=9){
    while(abs(frontCentreSensorMedian - calibratedFrontCentreDist) > 0.1 && reposCount<15){
      //if(frontCentreSensorMedian>9){
      m2MovementCount=0; 
      m1MovementCount=0; 
      avgCount=0;
      if((frontCentreSensorMedian - calibratedFrontCentreDist) > 0){
        // Setting wheels to move robot forward
        digitalWrite(m1INA, HIGH);
        digitalWrite(m2INA, HIGH);
        digitalWrite(m1INB, LOW);
        digitalWrite(m2INB, LOW);
      }
      //else if(frontCentreSensorMedian<9){
      else if((frontCentreSensorMedian - calibratedFrontCentreDist) < 0){
        // Setting wheels to move robot backward
        digitalWrite(m1INA, LOW);
        digitalWrite(m2INA, LOW);
        digitalWrite(m1INB, HIGH);
        digitalWrite(m2INB, HIGH);
      }
      analogWrite(m1PWM, 0.15*255);
      analogWrite(m2PWM, 0.13*255);
      //sonicReading();
      
      while(avgCount<5){
        avgCount = (m2MovementCount+m1MovementCount)/2;
      }
      
      digitalWrite(m1INA, LOW);
      digitalWrite(m2INA, LOW);
      digitalWrite(m1INB, LOW);
      digitalWrite(m2INB, LOW);
      analogWrite(m2PWM, 255);
      analogWrite(m1PWM, 255);
      
      for(long startTime=millis();(millis()-startTime)<50;){
        //computeMedian();
        computeFrontRepositionMedian();
      }
      reposCount++;
    }
  }
}

void computeRightRepositionMedian(){
  static int index=0;
  // get reading
  rightFrontSensorArray[index] = rightFrontSensorReading();
  rightBackSensorArray[index] = rightBackSensorReading();
  // sort data     
  rightRepositionInsertionSort();
  
  rightFrontSensorMedian = rightFrontSensorArray[arraySize/2];
  rightBackSensorMedian = rightBackSensorArray[arraySize/2];
  
  index = (index+1)%arraySize;
}

void rightRepositionInsertionSort(){
  for(int i=1; i<arraySize; i++){
    for(int j=i; j>0;j--){
      if(rightFrontSensorArray[j]<rightFrontSensorArray[j-1]){
        double temp = rightFrontSensorArray[j];
        rightFrontSensorArray[j] = rightFrontSensorArray[j-1];
        rightFrontSensorArray[j-1] = temp;
      }
      if(rightBackSensorArray[j]<rightBackSensorArray[j-1]){
        double temp = rightBackSensorArray[j];
        rightBackSensorArray[j] = rightBackSensorArray[j-1];
        rightBackSensorArray[j-1] = temp;
      }
    }
  } 
}

void repositionRobotRightSide(){
  int reposCount=0; //This variable prevent infinite repositioning
  //while(rightFrontSensorMedian != rightBackSensorMedian && reposCount<15){
  while(abs(rightFrontSensorMedian - rightBackSensorMedian) > 0.4 && reposCount<15){
    m2MovementCount=0;
    m1MovementCount=0;
    avgCount=0;
    if(rightBackSensorMedian>rightFrontSensorMedian){  
      // set wheels to rotate left
      digitalWrite(m1INA, HIGH);
      digitalWrite(m2INA, LOW);
      digitalWrite(m1INB, LOW);
      digitalWrite(m2INB, HIGH);
    } else if(rightBackSensorMedian<rightFrontSensorMedian){
      // set wheels to rotate right
      digitalWrite(m1INA, LOW);
      digitalWrite(m2INA, HIGH);
      digitalWrite(m1INB, HIGH);
      digitalWrite(m2INB, LOW);
    }
    // start rotating
    analogWrite(m2PWM, 0.1*255);
    analogWrite(m1PWM, 0.13*255);

    while(avgCount<5){
      avgCount = (m2MovementCount+m1MovementCount)/2;
    }
    // Setting wheels to brake
    digitalWrite(m1INB, LOW);
    digitalWrite(m2INB, LOW);
    digitalWrite(m1INA, LOW);
    digitalWrite(m2INA, LOW);
    analogWrite(m2PWM, 0.5*255);
    analogWrite(m1PWM, 0.5*255);
    
    for(long startTime=millis();(millis()-startTime)<50;){
      computeRightRepositionMedian();
    }
    reposCount++;
  } 
}

//-------------Front Sensors-------------------------------
double frontRightSensorReading(){
  //return ((4600/(analogRead(frontRightSensorPin)-10))-3);
  double dist = analogRead(frontRightSensorPin);
  return pow(3027.4 / dist, 1.2134); 
}

double frontLeftSensorReading(){
  //return ((4600/(analogRead(frontLeftSensorPin)-10))-3);
  double dist = analogRead(frontLeftSensorPin);
  return pow(3027.4 / dist, 1.2134);  
}

double frontCentreSensorReading(){
  //return ((4600/(analogRead(frontLeftSensorPin)-10))-3);
  double dist = analogRead(frontCentreSensorPin);
  return pow(3027.4 / dist, 1.2134);  
}

//-------------Right Sensors-------------------------------
double rightFrontSensorReading(){
  //return ((5000/(analogRead(rightFrontSensorPin)-10))-3);
  double dist = analogRead(rightFrontSensorPin);
  return pow(3027.4 / dist, 1.2134);
}

double rightBackSensorReading(){
  //return ((5000/(analogRead(rightBackSensorPin)-10))-3);
  double dist = analogRead(rightBackSensorPin);
  return pow(3027.4 / dist, 1.2134);
}

//-------------Left Sensors-------------------------------

double leftSensorReading(){
  //return ((5000/(analogRead(leftSensorPin)-10))-3);
  double dist = analogRead(leftSensorPin);
  return 65 * pow((dist * (5.0 / 1023.0)), -1.10);
}

//-------------Motor Interrupt----------------------------
void compute_m1_ticks(){
  m1Ticks++;
  m1MovementCount++;
}

void compute_m2_ticks(){
  m2Ticks++;
  m2MovementCount++;
}

void oneGridForward()
{  
  // Set duty cycle for M1=220 and M2=195 (RIGHT MOTOR HAS LESSER TICKS M1)
  m1DC=302;
  m2DC=302;
      
  for(m2MovementCount=0, m1MovementCount=0, m1Ticks=0, m2Ticks=0, avgCount=0; avgCount<235;)
  {
    forward();
    avgCount = (m2MovementCount+m1MovementCount)/2;
  }
  
  digitalWrite(m1INA, LOW);
  digitalWrite(m1INB, LOW);
  
  digitalWrite(m2INA, LOW);
  digitalWrite(m2INB, LOW);
 
  analogWrite(m1PWM, 0.86*255);
  analogWrite(m2PWM, 0.95*255);
}

void forward(){    
    digitalWrite(m1INA, HIGH);
    digitalWrite(m1INB, LOW);
      
    digitalWrite(m2INA, HIGH);
    digitalWrite(m2INB, LOW);
    // Setting wheels to move
    analogWrite(m1PWM, m1DC/1000 * 255);
    analogWrite(m2PWM, m2DC/1000 * 255);
    
    m1Ticks = 0;
    m2Ticks = 0;
}

void rotateRight90(){
  m1DC = 300;
  m2DC = 300;
  
  long startTime=millis();
  
  for(m2MovementCount=0, m1MovementCount=0, m1Ticks=0, m2Ticks=0, avgCount=0; avgCount<356 || (millis()-startTime)<457;){
    rotateRight();
    avgCount = (m2MovementCount+m1MovementCount)/2;
  }
  
  //LOW brake
  digitalWrite(m1INA, LOW);
  digitalWrite(m1INB, LOW);
  
  digitalWrite(m2INA, LOW);
  digitalWrite(m2INB, LOW);
 
  analogWrite(m1PWM, 255);
  analogWrite(m2PWM, 255);
}

void rotateLeft90(){
  m1DC = 300;
  m2DC = 300;
  
  long startTime=millis();
 
  for(m2MovementCount=0, m1MovementCount=0, m1Ticks=0, m2Ticks=0, avgCount=0; avgCount<355 || (millis()-startTime)<460;){
    rotateLeft();
    avgCount = (m2MovementCount+m1MovementCount)/2;
  }
  
  //LOW brake
  digitalWrite(m1INA, LOW);
  digitalWrite(m1INB, LOW);
  
  digitalWrite(m2INA, LOW);
  digitalWrite(m2INB, LOW);
 
  analogWrite(m1PWM, 255);
  analogWrite(m2PWM, 255);
}

void rotateRight(){    
    digitalWrite(m1INA, LOW);
    digitalWrite(m1INB, HIGH);
      
    digitalWrite(m2INA, HIGH);
    digitalWrite(m2INB, LOW);
    // Setting wheels to move
    analogWrite(m1PWM, m1DC/1000 * 255);
    analogWrite(m2PWM, m2DC/1000 * 255);
    
    m1Ticks = 0;
    m2Ticks = 0;
}

void rotateLeft(){    
    digitalWrite(m1INA, HIGH);
    digitalWrite(m1INB, LOW);
      
    digitalWrite(m2INA, LOW);
    digitalWrite(m2INB, HIGH);
    // Setting wheels to move
    analogWrite(m1PWM, m1DC/1000 * 255);
    analogWrite(m2PWM, m2DC/1000 * 255);
    
    m1Ticks = 0;
    m2Ticks = 0;
}

void moveForward(){  
    // Setting wheels to move
    analogWrite(m1PWM, m1DC/1000 * 255);
    analogWrite(m2PWM, m2DC/1000 * 255);
  
    unsigned long current_ms = millis();
  
    if((current_ms - prev_ms) > interval){
  
      m1Speed = (double)m1Ticks / ((current_ms - prev_ms)*1000);
      m2Speed = (double)m2Ticks / ((current_ms - prev_ms)*1000);
  
      m1Ticks = 0;
      m2Ticks = 0;
      
//      sumError += curError;
//      if (sumError > 0.01){
//        sumError = 0.01;
//      }
//      else if (sumError < 0.001){
//        sumError = 0.001;
//      }  
  
      curError = (m1Speed - m2Speed);
      dError = (curError - prevError);
      
      m2DC += ((curError*kP) - (dError*kD));
      if (m2DC > 1000){
        m2DC = 1000;
      }
      else if (m2DC < 0){
        m2DC = 0;
      }
                    
      prev_ms = current_ms;
      prevError = curError;     
    }
}
