package com.projectdark.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * [ADAPTED] Mobile action-icon painter reconstructed from user supplied original-game HUD references.
 * No screenshot pixels are shipped; icons are small vector/procedural reproductions so the runtime can
 * keep crisp scaling and clear pressed/disabled/selected states while preserving the original HUD vocabulary.
 */
final class ClassicHudIconAtlas {
  enum Icon { SLASH, WAVE, CLAW, BURST, AURA, FLAME, PALM, SPIRAL, BODY, COMET, TARGET, AUTO, MODE, ATTACK }

  void draw(Canvas c, Paint p, Icon icon, RectF box, boolean enabled, boolean selected, boolean pressed) {
    float inset=pressed?2f:0f;
    RectF b=new RectF(box.left+inset,box.top+inset,box.right-inset,box.bottom-inset);
    float radius=Math.max(4f,b.width()*.11f);

    // Dark bronze outer plate: all quick slots now read as one coherent classic deck.
    p.setStyle(Paint.Style.FILL);
    p.setColor(pressed?0xEF120F0E:0xE51B1613);
    c.drawRoundRect(b,radius,radius,p);
    p.setStyle(Paint.Style.STROKE);
    p.setStrokeWidth(selected?2.8f:1.5f);
    p.setColor(selected?0xFFFFD65D:(enabled?0xFF9D6C3E:0xFF4E4740));
    c.drawRoundRect(new RectF(b.left+.8f,b.top+.8f,b.right-.8f,b.bottom-.8f),radius,radius,p);

    // Inner recessed face + small top sheen removes the flat debug-button look.
    RectF face=new RectF(b.left+4,b.top+4,b.right-4,b.bottom-4);
    p.setStyle(Paint.Style.FILL);
    p.setColor(enabled?accentBackground(icon):0xC51B1B1D);
    c.drawRoundRect(face,Math.max(3f,radius-2f),Math.max(3f,radius-2f),p);
    p.setColor(enabled?0x30FFFFFF:0x12FFFFFF);
    c.drawRoundRect(new RectF(face.left+2,face.top+2,face.right-2,face.top+5),2,2,p);

    if(selected){
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.7f);p.setColor(0xDFFFF0A6);
      c.drawRoundRect(new RectF(face.left+1,face.top+1,face.right-1,face.bottom-1),4,4,p);
    }

    float cx=b.centerX(),cy=b.centerY(),w=b.width(),h=b.height();
    int fg=enabled?0xFFF4E9D0:0xFF77716A;
    int hot=enabled?0xFFFF7052:0xFF6D5550;
    int magic=enabled?0xFFB793FF:0xFF5E586B;
    int cool=enabled?0xFF74D6EA:0xFF58656B;
    p.setColor(icon==Icon.FLAME||icon==Icon.BURST||icon==Icon.COMET?hot:(icon==Icon.WAVE||icon==Icon.SPIRAL?magic:(icon==Icon.AURA?cool:fg)));
    Path path=new Path();
    p.setStyle(Paint.Style.FILL);

    switch(icon){
      case SLASH:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(cx-w*.27f,cy+h*.23f,cx+w*.24f,cy-h*.26f,p);
        p.setStrokeWidth(2);c.drawLine(cx-w*.12f,cy+h*.23f,cx+w*.30f,cy-h*.10f,p);break;
      case WAVE:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);path.moveTo(b.left+w*.18f,cy+h*.12f);path.cubicTo(cx-w*.10f,cy-h*.35f,cx+w*.02f,cy+h*.34f,b.right-w*.15f,cy-h*.18f);c.drawPath(path,p);break;
      case CLAW:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);for(int i=-1;i<=1;i++){float dx=i*w*.13f;path.reset();path.moveTo(cx+dx-w*.12f,cy+h*.25f);path.quadTo(cx+dx,cy-h*.05f,cx+dx+w*.10f,cy-h*.27f);c.drawPath(path,p);}break;
      case BURST:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);for(int i=0;i<8;i++){double a=Math.PI*2*i/8;float x1=cx+(float)Math.cos(a)*w*.10f,y1=cy+(float)Math.sin(a)*h*.10f;float x2=cx+(float)Math.cos(a)*w*.34f,y2=cy+(float)Math.sin(a)*h*.34f;c.drawLine(x1,y1,x2,y2,p);}p.setStyle(Paint.Style.FILL);c.drawCircle(cx,cy,w*.11f,p);break;
      case AURA:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawCircle(cx,cy,w*.23f,p);c.drawCircle(cx,cy,w*.10f,p);p.setStrokeWidth(1.5f);c.drawCircle(cx,cy,w*.31f,p);break;
      case FLAME:
        path.moveTo(cx,cy-h*.31f);path.cubicTo(cx+w*.28f,cy-h*.05f,cx+w*.18f,cy+h*.29f,cx,cy+h*.31f);path.cubicTo(cx-w*.25f,cy+h*.17f,cx-w*.17f,cy-h*.05f,cx,cy-h*.31f);c.drawPath(path,p);if(enabled){p.setColor(0xFFFFD86B);c.drawCircle(cx,cy+h*.10f,w*.08f,p);}break;
      case PALM:
        c.drawOval(new RectF(cx-w*.13f,cy-h*.02f,cx+w*.15f,cy+h*.27f),p);for(int i=-2;i<=2;i++){float fx=cx+i*w*.06f;c.drawRoundRect(new RectF(fx-w*.025f,cy-h*.28f,fx+w*.025f,cy+h*.02f),2,2,p);}break;
      case SPIRAL:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);RectF a=new RectF(cx-w*.24f,cy-h*.24f,cx+w*.24f,cy+h*.24f);c.drawArc(a,-50,285,false,p);c.drawCircle(cx+w*.05f,cy-h*.02f,w*.035f,p);break;
      case BODY:
        c.drawCircle(cx,cy-h*.18f,w*.09f,p);c.drawRoundRect(new RectF(cx-w*.10f,cy-h*.07f,cx+w*.10f,cy+h*.20f),5,5,p);c.drawRect(cx-w*.19f,cy,cx+w*.19f,cy+h*.06f,p);break;
      case COMET:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(cx-w*.28f,cy+h*.23f,cx+w*.10f,cy-h*.14f,p);p.setStyle(Paint.Style.FILL);c.drawCircle(cx+w*.18f,cy-h*.20f,w*.11f,p);break;
      case TARGET:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawCircle(cx,cy,w*.21f,p);c.drawLine(cx-w*.31f,cy,cx+w*.31f,cy,p);c.drawLine(cx,cy-h*.31f,cx,cy+h*.31f,p);break;
      case AUTO:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawArc(new RectF(cx-w*.24f,cy-h*.24f,cx+w*.24f,cy+h*.24f),35,265,false,p);path.reset();path.moveTo(cx+w*.20f,cy-h*.20f);path.lineTo(cx+w*.31f,cy-h*.07f);path.lineTo(cx+w*.13f,cy-h*.05f);c.drawPath(path,p);break;
      case MODE:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawCircle(cx-w*.09f,cy,w*.09f,p);c.drawCircle(cx+w*.12f,cy,w*.09f,p);c.drawLine(cx,cy-h*.18f,cx,cy+h*.18f,p);break;
      case ATTACK:
        // Dedicated sword button is deliberately more legible than a text label.
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(6);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(cx-w*.20f,cy+h*.24f,cx+w*.22f,cy-h*.23f,p);p.setStrokeWidth(4);c.drawLine(cx-w*.12f,cy+h*.08f,cx+w*.12f,cy+h*.28f,p);p.setStrokeWidth(2);c.drawLine(cx+w*.17f,cy-h*.28f,cx+w*.29f,cy-h*.16f,p);break;
    }

    // Functional-state cue at the bottom edge; disabled slots no longer look accidentally empty.
    p.setStyle(Paint.Style.FILL);
    p.setColor(enabled?0xA0E2B65F:0x70524B45);
    c.drawRoundRect(new RectF(b.left+7,b.bottom-4,b.right-7,b.bottom-2),1,1,p);
    p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
  }

  private int accentBackground(Icon icon){
    switch(icon){
      case FLAME:case BURST:case COMET:return 0xD33A1716;
      case WAVE:case SPIRAL:return 0xD323193F;
      case AURA:return 0xD3163039;
      case PALM:case BODY:return 0xD3262A1B;
      case AUTO:return 0xD31B2833;
      case ATTACK:return 0xD33B2018;
      default:return 0xD3221B19;
    }
  }
}