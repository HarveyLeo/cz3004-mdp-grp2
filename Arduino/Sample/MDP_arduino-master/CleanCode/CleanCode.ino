#include <math.h>
#include "DualVNH5019MotorShield.h"
#include "Motor.h"
#include "PinChangeInt.h"

#define L A3
#define LF A0
#define F A1
#define RF A2
#define RL A4
#define RS A5

DualVNH5019MotorShield md;

const float DISTANCE_LR_SENSOR = 17.6;
int getSensor = 1;

//note: setSpeeds is inversed. speed 1 is right speed 2 is left


void setup(){
  md.init();

  Serial.begin(115200); 

  PCintPort::attachInterrupt(motor1_A, Motor1EncoderA, CHANGE);
  PCintPort::attachInterrupt(motor1_B, Motor1EncoderB, CHANGE);
  PCintPort::attachInterrupt(motor2_A, Motor2EncoderA, CHANGE);
  PCintPort::attachInterrupt(motor2_B, Motor2EncoderB, CHANGE);
}

/* ---------------------------------- Encoder Interrupt Handler (Motor1EncoderA,Motor1EncoderB,Motor2EncoderA,Motor2EncoderB) ----------------------------------*/

void Motor1EncoderA(){
  motor1_Bnew ^ motor1_Aold ? motor1_encoder++ : motor1_encoder--;
  motor1_Aold = digitalRead(motor1_A);
}

void Motor1EncoderB(){
  motor1_Bnew = digitalRead(motor1_B);
  motor1_Bnew ^ motor1_Aold ? motor1_encoder++ : motor1_encoder--;

}

void Motor2EncoderA(){
  motor2_Bnew ^ motor2_Aold ? motor2_encoder++ : motor2_encoder--;
  motor2_Aold = digitalRead(motor2_A);
}

void Motor2EncoderB(){
  motor2_Bnew = digitalRead(motor2_B);
  motor2_Bnew ^ motor2_Aold ? motor2_encoder++ : motor2_encoder--;
}


//motor1=left,motor2=right
//move distance 1 ----- encoder 1125     actual distance 10 cm
//move distance 10 ------ encoder 11984    actual distance 99.3cm

void loop(){

  /* ---------------------------------- Serial Communication Handler ----------------------------------*/
  char command[10];
  int index = 0;
  char newChar;
  int param = 0;
  while (1){

    if (Serial.available()){
      newChar = Serial.read();
      command[index] = newChar;
      index++;
      if (newChar == '|'){
        index = 1;
        break;
      }
    }  
  }

  while (command[index] != '|'){
    param = param * 10 + command[index] - 48;
    index++;
  }

  char movement = command[0];
  switch ( movement ) {
  case 'W':
    {
      if (param == 0){
        moveForward(1);
      }
      else{
        moveForward(param);
      }    
      break;
    }
  case 'S':
    {
      if (param == 0){
        moveBackward(1);
      }
      else{
        moveBackward(param);
      }       
      break;
    }
  case 'A':
    {
      if (param == 0){
        turnLeft(90);
      }
      else{
        turnLeft(param);
      }       
      break;
    }
  
 
  case 'D':
    {
      if (param == 0){
        turnRight(90);
      }
      else{
        turnRight(param);
      }
      break;
    }
  case'E':
    {
      //test();

      for(int i=0; i<5; i++) {
        int adc = averageFeedback(15, 5, RF);
        Serial.println(adc);
        //Serial.println(6088.0 / (adc +7) - 2);
        //Serial.println(15878.0 / (adc +29) -7);
        Serial.println(5648.0 / (adc +7) - 1.7);
      }
        //calculateDistance(RL);
      break;
    }
  case 'Q': 
    {
      wallAlignment();
      break;
    }
  case 'X':
    {
      getSensor = 1;
      exportSensors();
      break;
    }
  case 'R':
    {
      getSensor = 0;
      break;
    }

  // code added by Jiachun to test whether arduino is connected to rpi, please don't delete
  case 'J':
    {
      Serial.write('C');
      break;
    }
  default:
    {
      break;
    }
    memset(command,0,sizeof(command));

  }

}

/* ---------------------------------- Testing and Later Stage Exploration ----------------------------------*/


int test(){
  while(1){
   delay(500);
   //Serial.println(calculateDistance(200));
   exportSensors();
   Serial.println();
  }
}



/* ---------------------------------- Standard Motor Function Call (Forward,Backward,Left,Right,Stop) ----------------------------------*/



int moveForward(int distance){

  motor1_encoder=0;
  motor2_encoder=0;  

  int multiplier;
  switch(distance){
//    case 1: multiplier = 1162; break;
    case 1: multiplier = 1148; break;
    case 2: multiplier = 1145; break;
    case 3: multiplier = 1157; break;
    case 4: multiplier = 1170; break;
    case 10: multiplier = 1185; break;
    case 11: multiplier = 1182; break;
    case 12: multiplier = 1182; break;
    
    default: multiplier = 1190; break;
  
  }
  int target_Distance = multiplier * distance;

  ///////////////////////
  // int target_Distance = ((2249/(6*3.142))*(distance*10));
  //  int target_Distance = 1192.9768*distance;
  // int target_Distance = 1150*distance;
  //int target_Distance = 1168.5*distance;
  // int left_offset=10.8;  //halfly charged white powerbank done
  int left_offset=265;    //fully charged 
  if (distance == 1){
    left_offset = 40;
  }
  /////////////////////

  int count=0;
  int pwm1=300, pwm2=300; 
  int output=0;
  int LeftPosition,RightPosition;



  while(1){
    LeftPosition = -motor1_encoder;    //hardcoded
    RightPosition = -motor2_encoder;  

    //Acceleration
    if(LeftPosition <=100){
      pwm1 = 100;
      pwm2 = 100;
    } 
    else if(LeftPosition >100 && LeftPosition <=300){
      pwm1 = LeftPosition;
      pwm2 = RightPosition;
    } 
    else {
      pwm1 = 300;
      pwm2 = 300;
    }   

    if(LeftPosition >= target_Distance-70){
      md.setBrakes(400, 400);
      delay(100);
      md.setBrakes(0, 0);
      break;
    }

    if(distance == 1){

      if(LeftPosition >= (target_Distance-70-200) && LeftPosition <= (target_Distance+100)){
        pwm1 = target_Distance-70-LeftPosition+100;
        pwm2 = target_Distance-70-LeftPosition+100;
      }
    }



    output = pidControlForward(motor1_encoder,motor2_encoder);
    md.setSpeeds(pwm1-output+left_offset, pwm2+output);

  }
  if(getSensor)
    exportSensors();
  
}



int moveBackward(int distance){

  motor1_encoder=0;
  motor2_encoder=0;  
  int target_Distance = ((2249/(6*3.142))*(distance*10));
  int left_offset=10.5;
  int count=0;
  int pwm1=300, pwm2=300; 
  int output=0;
  int LeftPosition,RightPosition;

  while(1){
    LeftPosition =  motor1_encoder;   //hardcoded
    RightPosition =  motor2_encoder;  

    //Acceleration
    if(LeftPosition <=100){
      pwm1 = 100;
      pwm2 = 100;
    } 
    else if(LeftPosition >100 && LeftPosition <=300){
      pwm1 =  LeftPosition;
      pwm2 =  RightPosition;
    } 
    else {
      pwm1 = 300;
      pwm2 = 300;
    }   

    if(LeftPosition >= target_Distance-70){

      md.setBrakes(400, 400);
      delay(100);
      md.setBrakes(0, 0);
      break;
    }
    if(distance == 1){
      if(LeftPosition >= (target_Distance-70-200) && LeftPosition <= (target_Distance+100)){
        pwm1 = target_Distance-70-LeftPosition+100;
        pwm2 = target_Distance-70-LeftPosition+100;
      }
    }


    output = pidControlBack(motor1_encoder,motor2_encoder);
    md.setSpeeds(-(pwm1-output)+left_offset, -(pwm2+output));
  } 
  if(getSensor)
    exportSensors();
}



int turnLeft(int angle){
  motor1_encoder=0;
  motor2_encoder=0;  
  int pwm1=300, pwm2=300, output=0,LeftPosition,RightPosition;
  int target_Angle;


  if (angle <= 5){
    target_Angle = angle * 12.50; 
    pwm1=150;
    pwm2=150;
  }   
  else if ((angle > 5) && (angle <= 15))
    target_Angle = angle * 18;
  else if ((angle > 15) && (angle <= 30))
    target_Angle = angle * 17.5;
  else if ((angle > 30) && (angle <= 45))
    target_Angle = angle * 14.55;

  //////////////////
  else if ((angle > 45) && (angle <= 90))   
    
    //target_Angle = angle * 17.89; 
  target_Angle = angle * 17.45;    
  ////////////////

  else  
    target_Angle = angle * 18.2;

  while(1){
    LeftPosition =  motor1_encoder; 
    RightPosition =  motor2_encoder; 

    if((LeftPosition >= target_Angle - 70)&&(angle > 8)){  //used to be rightPosition
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

    //  output = pidControlLeftAndRight(motor1_encoder,motor2_encoder); 
    output = pidControlLeftAndRight(LeftPosition,RightPosition); //hardcoded  

    //  md.setSpeeds(-(pwm1+output), pwm2-output);  
    md.setSpeeds((pwm1+output), -(pwm2-output)); //hardcoded

  } 

  if (angle == 90 && getSensor ==1)
    exportSensors();
}

int turnRight(int angle){
  motor1_encoder=0;
  motor2_encoder=0;  
  int pwm1=300, pwm2=300, output=0,LeftPosition,RightPosition;
  int target_Angle;
  // int target_Angle = angle * 18.0555;

  if (angle <= 15){
    target_Angle = angle * 17;  // 15 BASICALLY OKAY
    pwm1=150;
    pwm2=150;
  }
  else if ((angle > 15) && (angle <= 30))
    target_Angle = angle * 17;  // 30 BASICALLY OKAY
  else if ((angle > 30) && (angle <= 45))
    target_Angle = angle * 14.55;


  ///////////////////////
  else if ((angle > 45) && (angle <= 90))  
    //target_Angle = angle * 17.715;  // 90 OKAY on old arena
    // target_Angle = angle * 17.8;      // fully charged
    // target_Angle = angle * 17.82;
    target_Angle = angle * 17.48;

  //////////////////////
  else if ((angle > 90) && (angle <= 360))  
    target_Angle = angle * 17.9;
  else if ((angle > 360) && (angle < 720))  
    target_Angle = angle * 18.0;
  else if ((angle >= 720) && (angle <= 1080)) //OKAY
    target_Angle = angle * 18.1;
  else  
    target_Angle = angle * 18.1;

  while(1){

    LeftPosition =  motor1_encoder;
    RightPosition =  motor2_encoder;  

    //  output = pidControlLeftAndRight(motor1_encoder,motor2_encoder);
    output = pidControlLeftAndRight(LeftPosition,RightPosition); //hardcoded


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

  if (angle == 90 && getSensor == 1)
    exportSensors(); 
}





/* ---------------------------------- PID Control ----------------------------------*/


int pidControlForward(int LeftPosition, int RightPosition){
  int error,prev_error,pwm1=255,pwm2=255;
  float integral,derivative,output;
  //0.75
  float Kp = 0.75;  //0-0.1
  
  //1.65
  float Kd = 1.65;  //1-2
  
  //0.65
  float Ki = 0.75;  //0.5-1

  error = LeftPosition - RightPosition;
  integral += error;
  derivative = (error - prev_error);
  output = Kp*error + Ki * integral + Kd * derivative;
  prev_error = error;
  
  //Serial.println(error);

  pwm1=output;
  return pwm1;
}


int pidControlBack(int LeftPosition, int RightPosition){
  int error,prev_error,pwm1=255,pwm2=255;
  float integral,derivative,output;
  float Kp = 0.75;  //0-0.1
  float Kd = 1.65;  //1-2
  float Ki = 0.65;  //0.5-1

  error = RightPosition - LeftPosition;
  integral += error;
  derivative = (error - prev_error);
  output = Kp*error + Ki * integral + Kd * derivative;
  prev_error = error;


  pwm1=output; 
  return pwm1;
}

int pidControlLeftAndRight(int LeftPosition, int RightPosition){
  int error,prev_error,pwm1=255,pwm2=255;
  float integral,derivative,output;
  float Kp = 1;  //0-0.1
  float Kd = 0;  //1-2
  float Ki = 0;  //0.5-1

  error = RightPosition + LeftPosition;
  integral += error;
  derivative = (error - prev_error);
  output = Kp*error + Ki * integral + Kd * derivative;
  prev_error = error;

  pwm1=output;
  return pwm1;
}


/* ---------------------------------- Sensor Functions ----------------------------------*/
int irSensorFeedback (int sensorIndex){
  return analogRead(sensorIndex);
}

float calculateDistance(int sensorIndex){
  int numLoop = 5;
  int adc = averageFeedback(15,5,sensorIndex);
  float distance;
  float voltFromRaw;

  switch(sensorIndex){
  case LF: 
    distance = 6228.0 / (adc +15.5) - 3;
    break;
  case F:
    PWM_Mode_Setup();
    distance = 1;
    for(int i=0; i<4; i++) {
      int j = PWM_Mode();
      if(j > 10) {
        distance = -1;
        break;
      } 
    }
    md.init();
    break;

  case 100:
    distance = PWM_Mode();       
    break;

  case 200:
    PWM_Mode_Setup();
    distance = PWM_Mode();
    md.init(); 
    break;

  case RF: 
    distance = 5648.0 / (adc +7) - 1.7;
    break;
  case L: 
    distance = 6088 / (adc  + 7) - 1;
    break;
  case RL: 
    distance = 15878.0 / (adc +29) -7;
    break;
  case RS: 
    distance = 6088.0 / (adc +7) - 2;
    break;
  }
  return distance;
}

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

int averageFeedback(int in, int out, int pin){
  int x[in];
  int i;
  int sum = 0;
  int start = (in - out)/2;
  int average;
  for(i=0;i<in;i++){
    x[i] = irSensorFeedback(pin);
  }
  insertionsort(x, in);
  for(i = start; i < start+out; i++){
    sum = sum + x[i];
  }
  average = sum/out;
  return average;
}


/* ---------------------------------- Sending Out Sensor Data ----------------------------------*/

void exportSensors(){
  Serial.print(calculateDistance(L));
  Serial.print(",");
  Serial.print(calculateDistance(LF));  
  Serial.print(",");
  Serial.print(calculateDistance(F));
  Serial.print(",");
  Serial.print(calculateDistance(RF));
  Serial.print(",");
  Serial.print(calculateDistance(RL));
  Serial.print(",");
  Serial.print(calculateDistance(RS));
  Serial.print("|");
}


/* ---------------------------------- Left Wall Alignment ----------------------------------*/
void wallAlignment(){ 
  if ((calculateDistance(LF)-calculateDistance(RF)>10)||(calculateDistance(LF)-calculateDistance(RF)<-10)){
      return;
  } 
  alignAngel();
  delay(100);
  alignDistance();
  delay(100);
  alignAngel();
  delay(100);
}

void alignAngel(){
  
  int offset = 0;
  int frontLeftFeedback = averageFeedback( 30, 15, LF);
  int frontRightFeedback = averageFeedback(30, 15, RF);
  if (frontLeftFeedback < 235)
      offset = 12;
  int difference = frontLeftFeedback - frontRightFeedback + offset;

  while((difference > 5)||(difference < -5)){
    if ((calculateDistance(LF)-calculateDistance(RF)>10)||(calculateDistance(LF)-calculateDistance(RF)<-10)){
      break;
    }
    if (difference > 0)
      turnLeft(2);
    else if (difference < 0)
      turnRight(2);

    frontLeftFeedback = averageFeedback( 30, 15, LF);
    frontRightFeedback = averageFeedback( 30, 15, RF);
    difference = frontLeftFeedback - frontRightFeedback + offset;
  }
}


void alignDistance(){
      int near = 0;
      PWM_Mode_Setup();
      while(1) {
        
        if (averageFeedback(30,15,LF) < 365){
          if (calculateDistance(100) < 5)
             break;
          md.setSpeeds(100, 107);
        }
        else if(averageFeedback(30,15,LF) > 375 ){
          near = 1;
          md.setSpeeds(-100,-115);
          delay(70);
          md.setBrakes(400, 400);
          delay(100);
          md.setBrakes(0, 0);
        }
        else{
          break;
        }
      }
      if (near){
        md.setSpeeds(-70,-78);
        delay(200);
      }
      md.setBrakes(400, 400);
      delay(50);
      md.setBrakes(0, 0);
      md.init();
}

/* ------- Ultrasonic --------*/
int URPWM = 12; // PWM Output 0√î¬∫√ß25000US√î¬∫√•Every 50US represent 1cm
int URTRIG= 6; // PWM trigger pin


uint8_t EnPwmCmd[4]={
  0x44,0x02,0xbb,0x01};    // distance measure command

void PWM_Mode_Setup(){ 
  pinMode(URTRIG,OUTPUT);                     // A low pull on pin COMP/TRIG
  digitalWrite(URTRIG,HIGH);                  // Set to HIGH

    pinMode(URPWM, INPUT);                      // Sending Enable PWM mode command
}

int PWM_Mode(){                              // a low pull on pin COMP/TRIG  triggering a sensor reading
  unsigned int Distance=0;
  digitalWrite(URTRIG, LOW);
  digitalWrite(URTRIG, HIGH);               // reading Pin PWM will output pulses

  unsigned long DistanceMeasured  = pulseIn(URPWM,LOW);//a /30;

  while(DistanceMeasured>=10000){              // the reading is invalid.
    digitalWrite(URTRIG, LOW);
    digitalWrite(URTRIG, HIGH);
    DistanceMeasured  = pulseIn(URPWM,LOW);
  }

  Distance=DistanceMeasured/50;           // every 50us low level stands for 1cm

  return Distance;
}



