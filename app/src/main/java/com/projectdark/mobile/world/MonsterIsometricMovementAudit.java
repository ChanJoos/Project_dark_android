package com.projectdark.mobile.world;

/** Android-free regression audit for World-owned monster locomotion quantization. */
public final class MonsterIsometricMovementAudit {
  private MonsterIsometricMovementAudit(){}

  public static boolean verify(){
    float[][] requests={
        {10f,1f},{10f,-1f},{-10f,1f},{-10f,-1f},
        {0f,7f},{0f,-7f},{7f,0f},{-7f,0f}
    };
    for(float[] request:requests){
      MonsterIsometricMovementPolicy.Step step=MonsterIsometricMovementPolicy.project(request[0],request[1]);
      if(step.isZero()||!MonsterIsometricMovementPolicy.isLegalDiagonal(step.dx,step.dy))return false;
      float requestedMagnitude=(float)Math.sqrt(request[0]*request[0]+request[1]*request[1]);
      float projectedMagnitude=(float)Math.sqrt(step.dx*step.dx+step.dy*step.dy);
      if(Math.abs(requestedMagnitude-projectedMagnitude)>.001f)return false;
      MonsterIsometricMovementPolicy.Step detourX=MonsterIsometricMovementPolicy.detourX(step);
      MonsterIsometricMovementPolicy.Step detourY=MonsterIsometricMovementPolicy.detourY(step);
      if(!MonsterIsometricMovementPolicy.isLegalDiagonal(detourX.dx,detourX.dy))return false;
      if(!MonsterIsometricMovementPolicy.isLegalDiagonal(detourY.dx,detourY.dy))return false;
    }
    MonsterIsometricMovementPolicy.Step zero=MonsterIsometricMovementPolicy.project(0f,0f);
    if(!zero.isZero()||!MonsterIsometricMovementPolicy.isLegalDiagonal(zero.dx,zero.dy))return false;
    return true;
  }

  public static void main(String[] args){
    if(!verify())throw new AssertionError("MonsterIsometricMovementAudit failed");
    System.out.println("MonsterIsometricMovementAudit PASS");
  }
}
