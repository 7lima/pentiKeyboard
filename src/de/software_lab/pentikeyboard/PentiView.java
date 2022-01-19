// 18dec21 Software Lab. Alexander Burger

package de.software_lab.pentikeyboard;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

import android.util.*;
import android.text.*;
import android.view.*;
import android.graphics.*;
import android.content.res.*;
import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ClipboardManager;
import android.content.ClipDescription;
import android.content.ClipData;
import android.media.AudioManager;
import android.provider.Settings;
import android.os.Environment;
import android.content.pm.PackageManager;

public class PentiView extends View {
   PentiIME Ime;
   Blob[] Blobs;
   MotionEvent Ev;
   int Chord, Shift, Punct, Digit, Cntrl, AltGr, Funct;
   long Beg, End;
   boolean Off, Rev;
   CharSequence Clip2;
   int Def, Num, Repeat, Repeat2, Repeat3, Rpt, RptN, Vis, Cnt2;
   float BegX, BegY;
   Paint Text1 = new Paint();
   Paint Text2 = new Paint();
   Paint Circle1 = new Paint();
   Paint Circle2 = new Paint();
   Paint CircleFill = new Paint();
   File KeyDefs;
   static final int CANDIDATES = 40;
   String Dict[], Candidates[] = new String[CANDIDATES];
   float CandX[] = new float[CANDIDATES];
   float CandY;
   int CandPos;
   Paint Help;

   static final int CHORD = 80;
   static final int REPEAT = 80;

   final static int PentiMap[] = new int[] {
      0, 18, 22, 10, 17, 4, 24, 3, 25, 2, 9, 15, 12, 7,
      1, 6, 30, 26, 20, 8, 23, 14, 11, 31, 27, 19, 28
   };

   final static int Penti[] = new int[] {
      0, 'n', 'i', 'g', 'e', 0, 'o', 'm',
      's', 'j', 'c', 'v', 'l', 0, 'u', 'k',
      32, 'd', 'a', 'y', 'r', 0, 'b', 't',
      'f', 'h', 'q', 'x', 'z', 0, 'p', 'w'
   };
   final static int PentiPunct[] = new int[] {
      0, ')', '!', '=', '[', 0, '|', '>',
      '*', ';', ']', '(', '_', 0, '&', '@',
      32, '/', '`', '^', '$', 0, '{', '%',
      '?', '#', '\'', '\\', '"', 0, '}', '<'
   };
   final static int PentiDigit[] = new int[] {
      0, -KeyEvent.KEYCODE_DPAD_RIGHT, '3', '9', '2', 0, '8', '-',
      '1', ':', ',', -KeyEvent.KEYCODE_DPAD_DOWN, '7', 0, '0', 0,
      32, '.', '6', -KeyEvent.KEYCODE_DPAD_UP, '5', 0, -KeyEvent.KEYCODE_DPAD_LEFT, '~',
      '4', -KeyEvent.KEYCODE_MOVE_HOME, -KeyEvent.KEYCODE_PAGE_UP, -KeyEvent.KEYCODE_PAGE_DOWN, -KeyEvent.KEYCODE_MOVE_END, 0, '+', -KeyEvent.KEYCODE_INSERT
   };
   final static int PentiAltGr[] = new int[] {
      0, 'ñ', 'í', 'ĝ', 'é', 0, 'ö', '—',
      'ß', 'ĵ', 'ĉ', 'ŭ', '☺', 0, 'ü', 0,
      32, 'Δ', 'ä', 'ÿ', '€', 0, 'å', '☹',
      '¿', 'ĥ', 0, '±', 'ŝ', 0, '§', '♥'
   };
   final static int PentiFunct[] = new int[] {
      0, 0x10004E, -KeyEvent.KEYCODE_F3, -KeyEvent.KEYCODE_F9, -KeyEvent.KEYCODE_F2, 0, -KeyEvent.KEYCODE_F8, -KeyEvent.KEYCODE_F12,
      -KeyEvent.KEYCODE_F1, 0, 0x100043, 0, -KeyEvent.KEYCODE_F7, 0, -KeyEvent.KEYCODE_F10, 0x10004B,
      0x100020, 0x100044, -KeyEvent.KEYCODE_F6, 0, -KeyEvent.KEYCODE_F5, 0, -KeyEvent.KEYCODE_BREAK, 0,
      -KeyEvent.KEYCODE_F4, 0x100048, 0x100051, 0, -KeyEvent.KEYCODE_F11, 0, 0x100050, 0x100057
   };

   final static String PentiHelp1[] = new String[] {
      "Chord", "S", "P", "D", "A", "F", "Arpeggio"
   };
   final static String PentiHelp[][] = new String[][] {
      {"# ----", "SPACE", "SPACE", "SPACE", "SPACE", "NEW", "# #---", "SHIFT"},
      {"# --#-", "A", "`", "6", null, "F6", "# -#--", "PUNCT"},
      {"# -##-", "B", "{", "LEFT", null, "BREAK", "# --#-", "DIGIT"},
      {"- #-#-", "C", "]", ",", null, "COPY", "# ---#", "CNTRL"},
      {"# ---#", "D", "/", ".", null, "DEF", "- #--#", "ALTGR"},
      {"- -#--", "E", "[", "2", null, "F2", "- -##-", "FUNCT"},
      {"# #---", "F", "?", "4", null, "F4"},
      {"- --##", "G", "=", "9", null, "F9", "- #-#-", "RET/ESC"},
      {"# #--#", "H", "#", "HOME", null, "HELP", "- ##--", "TAB/DEL"},
      {"- --#-", "I", "!", "3", null, "F3"},
      {"- #--#", "J", ";", ":", null, null, "# ##--", "^Z"},
      {"- ####", "K", "@", null, null, "RESET", "# -##-", "^B"},
      {"- ##--", "L", "_", "7", null, "F7", "# --##", "^Y"},
      {"- -###", "M", ">", "-", null, "F12", "- ####", "^K"},
      {"- ---#", "N", ")", "RIGHT", null, "NUM"},
      {"- -##-", "O", "|", "8", null, "F8"},
      {"# ###-", "P", "}", "+", null, "PASTE"},
      {"# #-#-", "Q", "'", "PGUP", null, "QUIT"},
      {"# -#--", "R", "$", "5", null, "F5"},
      {"- #---", "S", "*", "1", null, "F1"},
      {"# -###", "T", "%", "~"},
      {"- ###-", "U", "&", "0", null, "F10"},
      {"- #-##", "V", "(", "DOWN"},
      {"# ####", "W", "<", "INS", null, "PASTE2"},
      {"# #-##", "X", "\\", "PGDOWN"},
      {"# --##", "Y", "^", "UP"},
      {"# ##--", "Z", "\"", "END", null, "F11"}
   };

   public PentiView(Context context, AttributeSet attrs) {
      super(context, attrs);
      Text1.setColor(Color.BLACK);
      Text1.setStyle(Paint.Style.STROKE);
      Text1.setTextAlign(Paint.Align.CENTER);
      Text2.setColor(Color.WHITE);
      Text2.setTextAlign(Paint.Align.CENTER);
      Circle1.setColor(Color.BLACK);
      Circle1.setPathEffect(new DashPathEffect(new float[]{7,7}, 0));
      Circle1.setStyle(Paint.Style.STROKE);
      Circle1.setStrokeWidth(4);
      Circle2.setColor(Color.WHITE);
      Circle2.setPathEffect(new DashPathEffect(new float[]{7,7}, 7));
      Circle2.setStyle(Paint.Style.STROKE);
      Circle2.setStrokeWidth(4);
      try {
         if ((KeyDefs = new File(context.getFilesDir().getPath() + "/KeyDefs")).exists()) {
            BufferedReader in = new BufferedReader(new FileReader(KeyDefs));

            for (int i = 0; i < PentiAltGr.length; ++i)
               PentiAltGr[i] = Integer.parseInt(in.readLine());
            in.close();
         }
      }
      catch (Exception e) {KeyDefs.delete();}
   }

   @Override public boolean onTouchEvent(MotionEvent ev) {
      Ev = ev;
      switch (ev.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
         Beg = ev.getEventTime();
         BegX = ev.getX();
         BegY = ev.getY();
         Chord = 0;
         Rev = false;
      case MotionEvent.ACTION_POINTER_DOWN:
         if (Blobs != null) {
            int p = ev.getActionIndex();
            float x = ev.getX(p);
            float y = ev.getY(p);
            int i;

            End = ev.getEventTime();
            if (Candidates[0] != null  &&  y < CandY) {
               for (i = 0;  i < CANDIDATES;  ++i)
                  if (x < CandX[i]) {
                     String s = Candidates[i];
                     int c = s.codePointAt(0);

                     if (Def == 0)
                        Def = -1;
                     else if (Def > 0)
                        defKey(c);
                     else {
                        if (c != Repeat) {
                           Repeat3 = Repeat2;
                           Repeat2 = Repeat;
                           Repeat = c;
                        }
                        Vis = c;
                        text(s);
                     }
                     break;
                  }
               for (i = 0;  i < CANDIDATES;  ++i)
                  Candidates[i] = null;
            }
            else {
               boolean missed = false;

               for (i = 0; i < 6; ++i) {
                  if (Blobs[i].contains(x, y)) {
                     if (i == 5) {
                        (new Thread() {
                           public void run() {
                              if (Repeat != 0) {
                                 int r = Rpt = ++RptN;

                                 sendRpt();
                                 try {
                                    sleep(REPEAT * 3);
                                    while (Rpt > 0  &&  Rpt == r) {
                                       sendRpt();
                                       sleep(REPEAT);
                                    }
                                 }
                                 catch (InterruptedException e) {}
                              }
                           }
                        } ).start();
                     }
                     else {
                        int n = 1 << i;

                        if ((Chord & n-1) != 0)
                           Rev = true;
                        Chord |= n;
                     }
                     break;
                  }
                  if (Blobs[i].closemiss(x, y))
                     missed = true;
               }
               if (i == 6) {
                  if (missed)
                     feedback();
                  Off = true;
                  Vis = 0;
                  if (Help == null)
                     Ime.setCandidatesViewShown(false);
               }
            }
         }
         break;
      case MotionEvent.ACTION_MOVE:
         if ((Blobs == null  ||  Off && Chord == 0)  &&  Settings.System.canWrite(Ime)) {
            float dx = ev.getX() - BegX;
            float dy = ev.getY() - BegY;
            if (Math.abs(dx) >= Math.abs(dy)) {
               if (dx <= -420  ||  420 <= dx) {
                  ((AudioManager)Ime.getSystemService(Context.AUDIO_SERVICE)).adjustVolume(dx>=420? 1 : -1, 0);
                  BegX += dx;
               }
            }
            else {
               ContentResolver cr = Ime.getContentResolver();
               Settings.System.putInt(cr,
                  Settings.System.SCREEN_BRIGHTNESS_MODE,
                  Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL );
               Settings.System.putInt(cr,
                  Settings.System.SCREEN_BRIGHTNESS,
                  Math.max(0, Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS, 64) - (int)(dy / 12)) );
            }
            BegY += dy;
         }
         break;
      case MotionEvent.ACTION_POINTER_UP:
         if (Rpt != 0)
            Rpt = -1;
         if (Blobs == null  &&  ev.getPointerCount() == 5  &&  (Blobs = init(ev)) == null)
            feedback();
         break;
      case MotionEvent.ACTION_UP:
         Ime.setCandidatesViewShown(true);
         if (Off || Rpt != 0) {
            Off = false;
            Chord = Rpt = 0;
         }
         else if (End - Beg >= CHORD  &&  ev.getEventTime() - End < CHORD * 3) {
            switch (Chord) {
            case 0x18:
               if (!Rev)
                  Shift = -1;
               else if (Shift == 0)
                  Shift = 1;
               else
                  Shift = 0;
               break;
            case 0x14:
               if (!Rev)
                  Punct = -1;
               else if (Punct == 0)
                  Punct = 1;
               else
                  Punct = 0;
               break;
            case 0x12:
               if (!Rev)
                  Digit = -1;
               else if (Digit == 0)
                  Digit = 1;
               else
                  Digit = 0;
               break;
            case 0x11:
               if (!Rev)
                  Cntrl = -1;
               else if (Cntrl == 0)
                  Cntrl = 1;
               else
                  Cntrl = 0;
               break;
            case 0x09:
               if (!Rev)
                  AltGr = -1;
               else if (AltGr == 0)
                  AltGr = 1;
               else
                  AltGr = 0;
               break;
            case 0x0A:
               send(Rev? -KeyEvent.KEYCODE_ESCAPE: -KeyEvent.KEYCODE_ENTER);
               reset();
               break;
            case 0x0C:
               send(Rev? -KeyEvent.KEYCODE_DEL : -KeyEvent.KEYCODE_TAB);
               break;
            case 0x06:
               if (!Rev)
                  Funct = -1;
               else if (Funct == 0)
                  Funct = 1;
               else
                  Funct = 0;
               break;
            case 0x1C:
               send('Z' & 0x1F);
               break;
            case 0x16:
               send('B' & 0x1F);
               break;
            case 0x13:
               send('Y' & 0x1F);
               break;
            case 0x0F:
               send('K' & 0x1F);
               break;
            default:
               chord();
            }
         }
         else
            chord();
      case MotionEvent.ACTION_CANCEL:
         Ev = null;
         break;
      }
      postInvalidate();
      return true;
   }

   void chord() {
      int c;

      if (Num >= 0)                    // Direct keycode
         c = PentiDigit[Chord];
      else if (Funct < 0)              // Single shots
         c = PentiFunct[Chord];
      else if (Punct < 0) {
         c = PentiPunct[Chord];
         if (Cntrl != 0)
            c &= 0x1F;
      }
      else if (Digit < 0)
         c = PentiDigit[Chord];
      else if (AltGr < 0) {
         c = PentiAltGr[Chord];
         if (c != 0  &&  Shift != 0)
            c = Character.toUpperCase((char)c);
      }
      else if (Cntrl < 0) {
         if ((c = Penti[Chord]) == 32)
            c = 0x100000;
         else
            c &= 0x1F;
      }
      else if (Shift < 0) {
         c = Character.toUpperCase((char)Penti[Chord]);
         if (Cntrl != 0)
            c &= 0x1F;
      }
      else if (Funct != 0)             // Locks
         c = PentiFunct[Chord];
      else if (Punct != 0) {
         c = PentiPunct[Chord];
         if (Cntrl != 0)
            c &= 0x1F;
      }
      else if (Digit != 0)
         c = PentiDigit[Chord];
      else if (AltGr != 0) {
         c = PentiAltGr[Chord];
         if (c != 0  &&  Shift != 0)
            c = Character.toUpperCase((char)c);
      }
      else {
         c = Penti[Chord];
         if (Cntrl != 0)
            c &= 0x1F;
         else if (c != 0  &&  Shift != 0)
            c = Character.toUpperCase((char)c);
      }
      send(c);
      if (Shift < 0)
         Shift = 0;
      if (Punct < 0)
         Punct = 0;
      if (Digit < 0)
         Digit = 0;
      if (Cntrl < 0)
         Cntrl = 0;
      if (AltGr < 0)
         AltGr = 0;
      if (Funct < 0)
         Funct = 0;
   }

   void send(int c) {
      if (Chord != 0) {
         setBackgroundResource(0);
         Help = null;
      }
      if (c != 0  &&  c != Repeat) {
         Repeat3 = Repeat2;
         Repeat2 = Repeat;
         if (Num < 0)
            Repeat = c;
      }
      send1(c);
   }

   void send1(int c) {
      if (Num >= 0) {
         if (c == -KeyEvent.KEYCODE_DEL) {
            Num /= 10;
            return;
         }
         if (c >= '0'  &&  c <= '9') {
            Num = Num * 10 + c - '0';
            return;
         }
         if (c != 0) {
            if ((c = Num) != Repeat)
               Repeat = c;
            Num = -1;
         }
      }
      if ((Vis = c) == 0)
         reset();
      else {
         ClipboardManager cm;
         CharSequence s;

         if (Candidates[0] != null) {
            int i, j;

            if (c == 0x100000)
               for (i = 0;  i < CANDIDATES;  ++i)
                  Candidates[i] = null;
            else {
               if (c == -KeyEvent.KEYCODE_TAB)
                  ++CandPos;
               else if (c == -KeyEvent.KEYCODE_DEL) {
                  if (CandPos > 1)
                     --CandPos;
                  else if ((i = Candidates[0].length()) > 0)
                     Candidates[0] = Candidates[0].substring(0, i-1);
               }
               else if (c >= (Candidates[0].length() > 0? 32 : 33)  &&  c < 0x100000) {
                  Candidates[0] = Candidates[0] + (char)c;
                  if (Dict == null) {
                     try {
                        BufferedReader rd = new BufferedReader(
                           new InputStreamReader(
                              getResources().openRawResource(R.raw.dict),
                              StandardCharsets.UTF_8 ) );

                        Dict = new String[Integer.parseInt(rd.readLine())];
                        for (i = 0;  i < Dict.length;  ++i)
                           Dict[i] = rd.readLine();
                        rd.close();
                     }
                     catch (IOException e) {Dict = null;}
                  }
               }
               j = 1;
               if (Candidates[0].length() > 0) {
                  int a = 0;
                  int z = Dict.length - 1;

                  while (a <= z) {
                     i = (a + z) / 2;
                     if (Dict[i].startsWith(Candidates[0])) {
                        while (i > 0  &&  Dict[i-1].startsWith(Candidates[0]))
                           --i;
                        do
                           Candidates[j++] = Dict[i].substring(Dict[i].indexOf('\t') + 1);
                        while (j < CANDIDATES  &&  ++i < Dict.length  &&  Dict[i].startsWith(Candidates[0]));
                        break;
                     }
                     if (Dict[i].compareTo(Candidates[0]) > 0)
                        z = i - 1;
                     else
                        a = i + 1;
                  }
               }
               while (j < CANDIDATES)
                  Candidates[j++] = null;
            }
         }
         else if (c < 0) {
            if (Def >= 0)
               Def = -1;
            else
               Ime.sendDownUpKeyEvents(-c);
         }
         else if (c < 0x100000) {
            if (Def == 0) {
               if ('A' <= c && c <= 'Z')
                  Def = (int)c - 64;
               else if ('a' <= c && c <= 'z')
                  Def = (int)c - 96;
               else
                  Def = -1;
            }
            else if (Def > 0)
               defKey(c == 32? 0 : c);
            else
               text((new StringBuffer()).appendCodePoint(c));
         }
         else {
            switch (c) {
            case 0x100000:  // CNTRL-SPACE
               Candidates[0] = "";
               CandPos = 1;
               break;
            case 0x100020:  // NEW (FUNCT-SPACE)
               Blobs = null;
               break;
            case 0x100043:  // COPY (FUNCT-C)
               cm = (ClipboardManager)Ime.getSystemService(Context.CLIPBOARD_SERVICE);
               if (cm != null) {
                  if ((s = Ime.getCurrentInputConnection().getSelectedText(0)) != null)
                     cm.setPrimaryClip(ClipData.newPlainText("Penti", s));
                  else if (cm.hasPrimaryClip()) {
                     s = cm.getPrimaryClip().getItemAt(0).coerceToText(getContext());
                     if (Clip2 == null)
                        Clip2 = s;
                     else {
                        Clip2 += " " + s;
                        ++Cnt2;
                     }
                  }
               }
               break;
            case 0x100044:  // DEF (FUNCT-D)
               Def = 0;
               break;
            case 0x100048:  // HELP (FUNCT-H)
               if (Help == null) {
                  Help = new Paint();
                  setBackgroundResource(R.drawable.help);
                  Help.setTextAlign(Paint.Align.CENTER);
               }
               break;
            case 0x10004B:  // RESET (FUNCT-K)
               Clip2 = null;
               Cnt2 = 0;
               reset();
               break;
            case 0x10004E:  // NUM (FUNCT-N)
               Num = 0;
               break;
            case 0x100050:  // PASTE (FUNCT-P)
               cm = (ClipboardManager)Ime.getSystemService(Context.CLIPBOARD_SERVICE);
               if (cm != null  &&  cm.hasPrimaryClip())
                  text(cm.getPrimaryClip().getItemAt(0).coerceToText(getContext()));
               break;
            case 0x100051:  // QUIT (FUNCT-Q)
               Ime.requestHideSelf(0);
               break;
            case 0x100057:  // Alternative PASTE (FUNCT-W)
               if (Clip2 != null) {
                  text(Clip2);
                  Clip2 = null;
                  Cnt2 = 0;
               }
               break;
            }
         }
      }
   }

   void sendRpt() {
      if (Repeat2 != 0  &&  (Chord & 0x01) != 0)
         send1(Repeat2);
      else if (Repeat3 != 0  &&  (Chord & 0x02) != 0)
         send1(Repeat3);
      else {
         if (Repeat2 != 0  &&  (Chord & 0x18) != 0) {
            if (Repeat3 != 0  &&  (Chord & 0x08) != 0)
               send1(Repeat3);
            send1(Repeat2);
         }
         send1(Repeat);
      }
   }

   void text(CharSequence s) {
      Ime.getCurrentInputConnection().commitText(s,1);
   }

   Blob[] init(MotionEvent ev) {
      Blob[] blobs = new Blob[6];
      boolean[] used = new boolean[5];
      int m, p, q;

      float max = 0;
      for (m = p = 0; p < 5; ++p)
         if (ev.getY(p) > max)
            max = ev.getY(m = p);
      used[m] = true;
      blobs[4] = new Blob(this, ev.getX(m), ev.getY(m));
      for (q = 3; q >= 0; --q) {
         float min = 2147483647;
         for (p = 0; p < 5; ++p) {
            if (!used[p]) {
               float d = blobs[q+1].dist(ev.getX(p), ev.getY(p));
               if (d < min) {
                  min = d;
                  m = p;
               }
            }
         }
         used[m] = true;
         blobs[q] = new Blob(this, ev.getX(m), ev.getY(m));
      }
      blobs[0].R = blobs[0].dist(blobs[1].X, blobs[1].Y) / 2;
      for (int i = 1; i <= 3; ++i)
         blobs[i].R = Math.min(
            blobs[i].dist(blobs[i-1].X, blobs[i-1].Y) / 2,
            blobs[i].dist(blobs[i+1].X, blobs[i+1].Y) / 2 );
      blobs[4].R = blobs[4].dist(blobs[3].X, blobs[3].Y) / 2;
      for (int i = 0; i <= 4; ++i)
         for (int j = 0; j <= 4; ++j)
            if (i != j  &&  blobs[i].dist(blobs[j].X, blobs[j].Y) < blobs[i].R + blobs[j].R)
               return null;
      blobs[5] = new Blob(this,
         ((blobs[0].X + blobs[4].X) / 2 + blobs[2].X) / 2,
         ((blobs[0].Y + blobs[4].Y) / 2 + blobs[2].Y) / 2 );
      blobs[5].R = Math.min(
         blobs[5].dist(blobs[1].X, blobs[1].Y) - blobs[1].R,
         Math.min(
            blobs[5].dist(blobs[2].X, blobs[2].Y) - blobs[2].R,
            blobs[5].dist(blobs[3].X, blobs[3].Y) - blobs[3].R ) );
      return blobs;
   }

   void feedback() {
      performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
      playSoundEffect(SoundEffectConstants.CLICK);
   }

   void fillCircle(Canvas canvas, float x, float y, float r) {
      CircleFill.setShader(new RadialGradient(x, y, r, Color.BLACK, Color.WHITE, Shader.TileMode.CLAMP));
      canvas.drawCircle(x, y, r, CircleFill);
   }

   @Override protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);

      float w = canvas.getWidth();
      float h = canvas.getHeight();

      if (Blobs != null) {
         String s;

         if (Help != null) {
            int c, pos[] = {7, 19, 30, 41, 52, 63, 79, 92};
            float n = h / (PentiHelp.length + 2);
            char buf[] = {0};

            Help.setTextSize(h > w? n / 2 : n * 3 / 4);
            Help.setColor(Color.RED);
            Help.setTypeface(Typeface.SERIF);
            for (int col = 0; col < PentiHelp1.length; ++col)
               canvas.drawText(PentiHelp1[col], w/100 * pos[col], n, Help);
            Help.setTypeface(Typeface.MONOSPACE);
            Help.setColor(Color.BLUE);
            for (int row = 0; row < PentiHelp.length; ++row)
               for (int col = 0; col < PentiHelp[row].length; ++col)
                  if (PentiHelp[row][col] != null)
                     canvas.drawText(PentiHelp[row][col], w/100 * pos[col], n + (row+1) * n, Help);
            for (int row = 1; row < PentiHelp.length; ++row)
               if ((c = PentiAltGr[PentiMap[row]]) != 0) {
                  buf[0] = (char)c;
                  canvas.drawText(buf, 0, 1, w/100 * pos[4], n + (row+1) * n, Help);
               }
         }
         else if ((s = Candidates[0]) != null) {
            Paint p = new Paint();
            float x, y, n;
            int i;

            p.setColor(Color.rgb(0xFF, 0xD5, 0x88));
            for (i = 0;  i < CANDIDATES;  ++i)
               CandX[i] = w;
            y = (w + h) / 2;
            canvas.drawRect(0, 0, w, CandY = y/20, p);
            p.setTypeface(Typeface.SERIF);
            p.setColor(Color.BLUE);
            p.setTextSize(y/25);
            canvas.drawText(s, x = 6, y/25, p);
            p.setStrokeWidth(4);
            n = p.measureText(s);
            for (i = 1; i < CandPos; ++i)
               CandX[i-1] = 6 + n + 12;
            for (i = CandPos;  i < CANDIDATES;  ++i) {
               if ((s = Candidates[i]) == null  ||  (x += n + 12) >= w)
                  break;
               CandX[i-1] = x;
               canvas.drawLine(x, 0, x, y/20, p);
               if ((x += 12) + (n = p.measureText(s)) >= w)
                  break;
               canvas.drawText(s, x, y/25, p);
            }
         }
         if (!Off) {
            if (Num >= 0) {               // Direct keycode
               s = Integer.toString(Num);
               if (Def > 0)
                  s = "DEF " + (char)(Def + 64) + " " + s;
            }
            else if (Def >= 0)
               s = Def > 0? "DEF " + (char)(Def + 64) : "DEF";
            else if (Funct < 0)           // Single shots
               s = "F";
            else if (Punct < 0)
               s = Cntrl == 0? "P" : "CP";
            else if (Digit < 0)
               s = "D";
            else if (AltGr < 0)
               s = Shift == 0? "A" : "SA";
            else if (Cntrl < 0)
               s = "C";
            else if (Shift < 0)
               s = "S";
            else if (Funct != 0)          // Locks
               s = "F";
            else if (Punct != 0)
               s = Cntrl == 0? "P" : "CP";
            else if (Digit != 0)
               s = "D";
            else if (AltGr != 0)
               s = Shift == 0? "A" : "SA";
            else if (Cntrl != 0)
               s = "C";
            else if (Shift != 0)
               s = "S";
            else if (Vis == -KeyEvent.KEYCODE_ESCAPE)
               s = "ESC";
            else if (Vis == -KeyEvent.KEYCODE_INSERT)
               s = "INS";
            else if (Vis == -KeyEvent.KEYCODE_BREAK)
               s = "BREAK";
            else if (Vis == 0x100043)
               s = Cnt2 == 0? "COPY" : "(" + Integer.toString(Cnt2 + 1) + ")";
            else if (Vis == 0x10004B)
               s = "RESET";
            else if (-KeyEvent.KEYCODE_F1 >= Vis  &&  Vis >= -KeyEvent.KEYCODE_F12)
               s = "F" + Integer.toString(1 - Vis - KeyEvent.KEYCODE_F1);
            else if (0 < Vis  &&  Vis < 32)
               s = "^" + (char)(Vis + 64);
            else
               s = "";
            if (s.length() != 0) {
               float x = Blobs[5].X;
               float y = Blobs[5].Y;
               float f = Blobs[2].dist(x, y) / 4;

               x = x + 1.75f * (Blobs[2].X - x);
               y = y + 1.75f * (Blobs[2].Y - y);
               Text1.setTextSize(f);
               Text1.setStrokeWidth(f / 8);
               canvas.drawText(s, x, y, Text1);
               Text2.setTextSize(f);
               canvas.drawText(s, x, y, Text2);
            }
            for (int i = 0; i < 6; ++i)
               Blobs[i].draw(canvas);
         }
      }
      else if (Ev != null) {
         if (Ev.getPointerCount() == 5) {
            Blob[] blobs;

            if ((blobs = init(Ev)) != null)
               for (int p = 0; p < 6; ++p)
                  fillCircle(canvas, blobs[p].X, blobs[p].Y, blobs[p].R);
         }
      }
      else {
         Paint p = new Paint();

         p.setTextSize(w / 4);
         p.setTypeface(Typeface.SERIF);
         p.setTextAlign(Paint.Align.CENTER);
         p.setShader(new LinearGradient(w/4, 0, w*3/4, 0, Color.BLACK, Color.WHITE, Shader.TileMode.CLAMP));
         canvas.drawText("Penti", w/2, h/2, p);
      }
   }

   void reset() {
      Shift = Punct = Digit = Cntrl = AltGr = Funct = 0;
      Def = Num = -1;
   }

   void defKey(int c) {
      PentiAltGr[PentiMap[Def]] = c;
      try {
         PrintWriter out = new PrintWriter(KeyDefs);

         for (int i = 0; i < PentiAltGr.length; ++i)
            out.println(PentiAltGr[i]);
         out.close();
      }
      catch (IOException e) {}
      Def = -1;
   }

   public class Blob {
      PentiView PV;
      float X, Y, R;

      Blob(PentiView pv, float x, float y) {
         PV = pv;
         X = x;
         Y = y;
      }

      public boolean contains(float x, float y) {
         float dx = x - X;
         float dy = y - Y;

         return Math.sqrt(dx * dx + dy * dy) <= R;
      }

      public boolean closemiss(float x, float y) {
         float dx = x - X;
         float dy = y - Y;

         return Math.sqrt(dx * dx + dy * dy) <= R + Blobs[5].R;
      }

      public void draw(Canvas canvas) {
         if (PV.Ev != null) {
            for (int p = Ev.getPointerCount(); --p >= 0;)
               if (contains(Ev.getX(p), Ev.getY(p))) {
                  fillCircle(canvas, X, Y, R);
                  return;
               }
         }
         canvas.drawCircle(X, Y, R, Circle1);
         canvas.drawCircle(X, Y, R, Circle2);
      }

      public int dist(float x, float y) {
         x -= X;
         y -= Y;
         return (int)Math.sqrt(x * x + y * y);
      }
   }
}
