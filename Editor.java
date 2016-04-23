package io.gitcafe.maxco292.editsharp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Lexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import io.gitcafe.maxco292.editsharp.CPP14.CPP14Lexer;
import io.gitcafe.maxco292.editsharp.JavaLang.JavaLexer;
import io.gitcafe.maxco292.editsharp.Lua.LuaLexer;
import io.gitcafe.maxco292.editsharp.Python3.Python3Lexer;
import io.gitcafe.maxco292.editsharp.adapter.MyAdapter;
import io.gitcafe.maxco292.editsharp.csharp.CSharp4Lexer;
import io.gitcafe.maxco292.editsharp.db.DatabaseAdapter;

import static java.lang.Character.isSpaceChar;

public class Editor extends AppCompatActivity {
    private String mPreValue[]={"0","1","2","3","4"};
    private EditText mtt;
    private ListView mLv;
    private FloatingActionButton fab_save;
    private CharSequence mCharS="NO STRING HERE NOW";
    private static boolean DELETE_MODIFY;
    private static boolean INSERT_MODIFY;
    private static boolean SUCCESS_MODEIFY;
    private static boolean AUTOCOMPLETE_MODIFY;
    private static boolean DBINSERT=true;
    public static int START;
    private static int MID_START;
    public static int END;
    private List<String> identifiers=new ArrayList<>();
    private List<String> testArray = new ArrayList<>();
    private MyAdapter adapter;
    private String typeoffile;
    private HashMap<String,Integer> typeID_HM=new HashMap<>();
    private String FilePath;
    private int toolbarheight;
    private int statusbarheight;
    private String FileName;
    private void setmPreValue(String[] str)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        str[0]=prefs.getString("remote_IP_text","");
        str[1]=prefs.getString("remote_Directory_text","");
        str[2]=prefs.getString("remote_username_text","");
        str[3]=prefs.getString("remote_passwords_text","");
        str[4]=prefs.getString("local_directory_text","");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setmPreValue(mPreValue);
        toolbarheight=dip2px(this,56);
        statusbarheight=getStatusBarHeight();
        typeID_HM.put("cs",107);
        typeID_HM.put("cpp",124);
        typeID_HM.put("py", 35);
        typeID_HM.put("c", 124);
        typeID_HM.put("h", 124);
        typeID_HM.put("lua", 50);
        typeID_HM.put("java", 100);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab_save= (FloatingActionButton) findViewById(R.id.fab_save);

        SUCCESS_MODEIFY=true;
        adapter = new MyAdapter(this, testArray);
        Intent mintent=getIntent();
        Uri uri=Uri.parse(mintent.getStringExtra("filedata"));
        FilePath=uri.getPath();
        fab_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File fi = new File(FilePath);
                FileOutputStream testfileio=null;
                try {
                    testfileio = new FileOutputStream(fi);
                    byte b[]=mtt.getText().toString().getBytes();
                    testfileio.write(b, 0, b.length);
                    testfileio.close();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                SSFTPsync.sshSftp(
                                        mPreValue[0],
                                        mPreValue[2],
                                        mPreValue[3],
                                        -1,
                                        mPreValue[1],
                                        FilePath,
                                        FileName
                                );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(Editor.this,"已保存并上传文件到远程服务器，请在服务器端查看;保存成功",Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        FileName=uri.getLastPathSegment();
        setTitle(FileName);
        mLv= (ListView) findViewById(R.id.list_all);
        mtt=(EditText)findViewById(R.id.note1);
        mtt.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                final String[] tx={""};
                final String[] encodingslist={"UTF-8","ANSI","UTF-8-BOM","GB2312","Shift-JIS"};
                final AlertDialog.Builder builder=new AlertDialog.Builder(Editor.this);
                builder.setTitle(R.string.single_chioce_list_dialogs_title_1);
                builder.setSingleChoiceItems(encodingslist, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
                                File fi = new File(FilePath);
                                InputStreamReader bufRead = null;
                                try {
                                    bufRead = new InputStreamReader(new FileInputStream(fi),encodingslist[which]);
                                } catch (FileNotFoundException | UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                StringBuilder sb = new StringBuilder();
                                char[] buffer = new char[2048];
                                int len = 0;
                                try {
                                   if(bufRead!=null) {
                                       while ((len = bufRead.read(buffer, 0, 2048)) > 0) {
                                           sb.append(buffer, 0, len);
                                       }
                                       try {
                                           bufRead.close();
                                       } catch (IOException e) {
                                           e.printStackTrace();
                                       }
                                   }
                                    else {
                                       Toast.makeText(Editor.this,"请选择正确的编码！",Toast.LENGTH_LONG).show();
                                   }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                String  text = sb.toString();
                                tx[0] =text;
                                SpannableString lll = Color_DFA(tx[0], typeoffile);
                                mtt.setText(lll);
                            }
                        });
                String positiveText = getString(android.R.string.ok);
                builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }
                );

                final AlertDialog alertDialog=builder.create();
                alertDialog.show();

                //Toast.makeText(Editor.this, "已保存并上传文件到远程服务器，请在服务器端查看", Toast.LENGTH_SHORT).show();
            }
        });
        TextWatcher tw=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("---***---", "beforeTextChanged 被执行----> s=" + s+"----start="+ start
                        + "----after="+after + "----count" +count);
                if(DBINSERT==true)//run only once
                {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseAdapter.getIntance(Editor.this).inserInfo(identifiers);
                            //Toast.makeText(Editor.this,"DATABASEUPDATE COMPLETED",Toast.LENGTH_SHORT).show();
                            Log.d("DBCOMPLETE","hahahahahaha");
                        }
                    }).start();
                    DBINSERT=false;
                }
                if(count>=1)
                {
                    DELETE_MODIFY=true;
                    INSERT_MODIFY=false;
                    START=(start-count-1)>=0?start-count-1:0;
                    MID_START=start;
                    END=start;
                    int length=s.length();
                    try {
                        char st = s.charAt(START);
                        char ed = s.charAt(END);
                        if (START != 0) {
                            while (!isSpaceChar(st) && st != '\n' && st != '\r' && START != 0) {
                                START -= 1;
                                st = s.charAt(START);
                            }
                            if (START != 0) {
                                START += 1;
                            }
                        }
                        while (!isSpaceChar(ed) && ed != '\n' && ed != '\r' && END != length) {
                            //未执行边界检查
                            END += 1;
                            ed = s.charAt(END);
                        }
                    }catch(Exception e)
                    {
                        mCharS="";
                    }
                    Log.d("bfc//////","START:"+START+"MID_START:"+MID_START);
                }
                else {
                    DELETE_MODIFY = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("---***---", "onTextChanged 被执行--------start=" + start
                        + "\n----before=" + before + "\n----count" + count);
                if(DELETE_MODIFY==false || AUTOCOMPLETE_MODIFY==true) {
                    if (count == 1) {
                        SUCCESS_MODEIFY = true;
                        START = start;
                        MID_START = start;
                        END = start + count;
                        INSERT_MODIFY = true;
                        DELETE_MODIFY = false;
                        //SUCCESS_MODEIFY = true;
                        //判断start是否为空
                        try {
                            char st = s.charAt(START);
                            char ed = s.charAt(END);
                            if (MID_START != 0) {
                                while (!isSpaceChar(st) && st != '\n' && st != '\r' && START != 0) {
                                    START -= 1;
                                    st = s.charAt(START);
                                }
                                if (START != 0) {
                                    START += 1;
                                }
                            }
                            //判断end是否为空
                            while (!isSpaceChar(ed) && ed != '\n' && ed != '\r' && END != s.length()) {
                                //未执行边界检查
                                END += 1;
                                ed = s.charAt(END);
                            }
                            mCharS = s.subSequence(START, MID_START + 1);
                        }catch (IndexOutOfBoundsException e)
                        {
                            e.printStackTrace();
                            mCharS="";
                        }
                        //Toast.makeText(Editor.this,mCharS,Toast.LENGTH_SHORT).show();
                    } else if (count > 1) {
                        SUCCESS_MODEIFY = true;
                        INSERT_MODIFY = true;
                        DELETE_MODIFY = false;
                        START = MID_START = start;
                        END = START + count;
                        char st = s.charAt(START);
                        char ed = s.charAt(END);
                        if (MID_START != 0) {
                            while (!isSpaceChar(st) && st != '\n' && st != '\r' && START != 0) {
                                START -= 1;
                                st = s.charAt(START);
                            }
                            if (START != 0) {
                                START += 1;
                            }
                        }
                        //判断end是否为空
                        int length = s.length();
                        while (!isSpaceChar(ed) && ed != '\n' && ed != '\r' && END != length) {
                            //未执行边界检查
                            END += 1;
                            ed = s.charAt(END);
                        }
                        mCharS = s.subSequence(START, MID_START + 1);
                    }
                }
                else
                {
                    INSERT_MODIFY=false;
                    SUCCESS_MODEIFY=true;
                    //Log.d("DELETE",s.toString());
                }

                try {
                    Log.d("前前后后", mCharS.toString());
                    Log.d("---ontcstr---", s.subSequence(START, END).toString());

                    //起始及结束位置，test专用
                    //Toast.makeText(Editor.this,"START:"+START+"*** END:"+END,Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {
                    Log.d("error:",e.getMessage());
                    Log.d("---ontc---","捕捉失败");
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("---***---", "afterTextChanged 被执行----");
//listview测试
                mtt.removeTextChangedListener(this);
                if(INSERT_MODIFY==true) {
                    try {
                        int pos = mtt.getSelectionStart();
                        Layout layout = mtt.getLayout();
                        int line = layout.getLineForOffset(pos);
                        int baseline = layout.getLineBaseline(line);
                        int ascent = layout.getLineAscent(line);
                        float x = layout.getPrimaryHorizontal(pos);
                        float y = baseline + ascent;

                        testArray = new ArrayList<String>();
                        testArray = DatabaseAdapter.getIntance(Editor.this)
                                .queryInfo(
                                        mCharS.toString());
                        adapter.refreshData(testArray);
                        mLv.setAdapter(adapter);
                        mLv.setBackgroundColor(Color.WHITE);
                        mLv.setX(x);
                        mLv.setY(y + toolbarheight + statusbarheight+(baseline/line)*2);
                        mLv.setVisibility(View.VISIBLE);
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
//
                if(SUCCESS_MODEIFY==true && START!=END) {
                    try {
                        Color_DFA(s, START, END);
                    }catch (Exception e)
                    {

                    }
                }
                mtt.addTextChangedListener(this);
            }
        };
        typeoffile=mintent.getStringExtra("type");
        try {
            fileOpen(uri,typeoffile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mtt.addTextChangedListener(tw);
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AUTOCOMPLETE_MODIFY = true;
                mtt.getText().insert(START + mCharS.length(), adapter.getItem(position));
                mtt.getText().delete(START, mCharS.length() + START - 1);
                mLv.setVisibility(View.GONE);
                AUTOCOMPLETE_MODIFY = false;
            }
        });
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void fileOpen(Uri uri, final String typeoffile) throws IOException {
        File fi = new File(FilePath);
        InputStreamReader bufRead = new InputStreamReader(new FileInputStream(fi));
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[2048];
        int len = 0;
        while ((len = bufRead.read(buffer, 0, 2048)) > 0) {
            sb.append(buffer, 0, len);
        }
        String text = sb.toString();
        bufRead.close();
        SpannableString lll=Color_DFA(text,typeoffile);
        mtt.setText(lll);
    }

    private SpannableString Color_DFA(String input_str,String typeoffile) //全文自动机
    {
        SpannableString lll=new SpannableString(input_str);
        Lexer cs=selectlexer(typeoffile,input_str);
        CommonToken cst= (CommonToken) cs.nextToken();
        String gt=cst.getText();
        int type=cst.getType();
        Pattern allletter=Pattern.compile("[A-Za-z]*");
        int ID_NUM=typeID_HM.get(typeoffile);
        while(gt!="<EOF>") {
            if(type==ID_NUM) {
                lll.setSpan(new ForegroundColorSpan(Color.RED), cst.getStartIndex(), cst.getStopIndex() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                identifiers.add(gt);
            }else
            {

                if(allletter.matcher(gt).matches())
                {
                    lll.setSpan(new ForegroundColorSpan(Color.BLUE), cst.getStartIndex(), cst.getStopIndex() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            cst= (CommonToken) cs.nextToken();
            gt=cst.getText();
            type=cst.getType();
        }
        return lll;
    }
    public  void Color_DFA(Editable s,int st,int ed) //小型自动机
    {
        START=st;
        END=ed;
        String tempstr=s.toString().substring(START,END);
        SpannableString lll=new SpannableString(tempstr);
        Lexer cs=selectlexer(typeoffile,tempstr);
        CommonToken cst= (CommonToken) cs.nextToken();
        String gt=cst.getText();
        int type=cst.getType();
        Pattern allletter=Pattern.compile("[A-Za-z]*");
        int ID_NUM=typeID_HM.get(typeoffile);
        while(gt!="<EOF>") {
            if(type==ID_NUM) {
                lll.setSpan(new ForegroundColorSpan(Color.RED), cst.getStartIndex(), cst.getStopIndex() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                identifiers.add(gt);
            }else
            {
                if(allletter.matcher(gt).matches())
                {
                    lll.setSpan(new ForegroundColorSpan(Color.BLUE), cst.getStartIndex(), cst.getStopIndex() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            cst= (CommonToken) cs.nextToken();
            gt=cst.getText();
            type=cst.getType();
        }
        s.replace(START, END, lll);
    }
    private Lexer selectlexer(String typeoffile,String input_str)
    {
        Lexer cs;
        switch (typeoffile)
        {
            case "cs":
                cs=new CSharp4Lexer(new ANTLRInputStream(input_str));
                break;
            case "cpp":
                cs=new CPP14Lexer(new ANTLRInputStream(input_str));
                break;
            case "py":
                cs=new Python3Lexer(new ANTLRInputStream(input_str));
                break;
            case "java":
                cs=new JavaLexer(new ANTLRInputStream(input_str));
                break;
            case "c":
                cs=new CPP14Lexer(new ANTLRInputStream(input_str));
                break;
            case "lua":
                cs=new LuaLexer(new ANTLRInputStream(input_str));
                break;
            default://.h CPP/C headers
                cs=new CPP14Lexer(new ANTLRInputStream(input_str));
        }
        return cs;
    }

}
