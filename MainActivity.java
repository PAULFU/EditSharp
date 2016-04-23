package io.gitcafe.maxco292.editsharp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.gitcafe.maxco292.editsharp.db.DatabaseAdapter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private String[] JAVA_LITERAL_NAMES = {
            "abstract", "assert", "boolean", "break", "byte", "case",
            "catch", "char", "class", "const", "continue", "default",
            "do", "double", "else", "enum", "extends", "final", "finally",
            "float", "for", "if", "goto", "implements", "import", "instanceof",
            "int", "interface", "long", "native", "new", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while",
            "null"
    };
    private String[] LUA_LITERAL_NAMES = {
            "break", "goto", "do", "end", "while", "repeat",
            "until", "if", "then", "elseif", "else", "for", "in",
            "function", "local", "return",  "nil", "false",
            "true"
    };
    private String[] PY3_LITERAL_NAMES = {
            "def", "return", "raise", "from", "import", "as", "global",
            "nonlocal", "assert", "if", "elif", "else", "while", "for",
            "in", "try", "finally", "with", "except", "lambda", "or",
            "and", "not", "is", "None", "True", "False", "class", "yield",
            "del", "pass", "continue", "break"
    };
    private   String[] CS_LITERAL_NAMES = {
            "abstract",
            "add", "alias", "__arglist", "as", "ascending", "base", "bool",
            "break", "by", "byte", "case", "catch", "char", "checked",
            "class", "const", "continue", "decimal", "default", "delegate",
            "descending", "do", "double", "dynamic", "else", "enum", "equals",
            "event", "explicit", "extern", "false", "finally", "fixed",
            "float", "for", "foreach", "from", "get", "goto", "group",
            "if", "implicit", "in", "int", "interface", "internal", "into",
            "is", "join", "let", "lock", "long", "namespace", "new",
            "null", "object", "on", "operator", "orderby", "out", "override",
            "params", "partial", "private", "protected", "public", "readonly",
            "ref", "remove", "return", "sbyte", "sealed", "select", "set",
            "short", "sizeof", "stackalloc", "static", "string", "struct",
            "switch", "this", "throw", "true", "try", "typeof", "uint",
            "ulong", "unchecked", "unsafe", "ushort", "using", "virtual",
            "void", "volatile", "where", "while", "yield"
    };
    private String[] CPP_LITERAL_NAMES = {
            "alignas", "alignof", "asm", "auto", "bool", "break",
            "case", "catch", "char", "char16_t", "char32_t", "class",
            "const", "constexpr", "const_cast", "continue", "decltype",
            "default", "delete", "do", "double", "dynamic_cast", "else",
            "enum", "explicit", "export", "extern", "false", "final",
            "float", "for", "friend", "goto", "if", "inline", "int",
            "long", "mutable", "namespace", "new", "noexcept", "nullptr",
            "operator", "override", "private", "protected", "public", "register",
            "reinterpret_cast", "return", "short", "signed", "sizeof", "static",
            "static_assert", "static_cast", "struct", "switch", "template",
            "this", "thread_local", "throw", "true", "try", "typedef",
            "typeid", "typename", "union", "unsigned", "using", "virtual",
            "void", "volatile", "wchar_t", "while"
    };
    private int FILE_SELECT_CODE=0;
    private FloatingActionButton fab;
    private FloatingActionButton fab_git;
    private AlertDialog extra_alertDialog;
    private MaterialDialog materialDialog;
    private String mPreValue[]={"0","1","2","3","4"};
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final int START=0;
    private static final int STOP=1;
    private ProgressBar mProgressBar;
    private Handler mHandler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case START:
                    mProgressBar.setVisibility(View.VISIBLE);break;
                case STOP:
                    mProgressBar.setVisibility(View.INVISIBLE);break;
            }
        }
    };

    private List<String> getListArray(String[] array) {
        List<String> titleArray = new ArrayList<>();
        for (String title : array) {
            titleArray.add(title);
        }
        return titleArray;
    }


    private void getPreValue(String[] str){
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
        str[0]=preferences.getString("remote_IP_text","");
        str[1]=preferences.getString("remote_Directory_text","");
        str[2]=preferences.getString("remote_username_text","");
        str[3]=preferences.getString("remote_passwords_text","");
        str[4]=preferences.getString("local_directory_text","");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri=getIntent().getData();
        getPreValue(mPreValue);
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        SSFTPsync.connectSftp(
                                mPreValue[0],
                                mPreValue[2],
                                mPreValue[3],
                                -1,
                                mPreValue[1]
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
           Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String[] items=SSFTPsync.strings;
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.single_choice_list_dialogs_title);

        //list of items
        builder.setSingleChoiceItems(items, -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        // item selected logic
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                               // mHandler.obtainMessage(START).sendToTarget();
                                String RemoteFileName;
                                RemoteFileName = items[which];
                                File fi = new File(Environment
                                        .getExternalStorageDirectory().getPath()
                                        + mPreValue[4]);
                                if (!fi.exists() && !fi.isDirectory()) {
                                    System.out.println("//不存在");
                                    fi.mkdir();
                                }
                                File fiLF = new File(fi.getPath() + File.separator + RemoteFileName);
                                Log.d("fileLOCALPATH", fiLF.toString());
                                if (!fiLF.exists()) {
                                    try {
                                        fiLF.createNewFile();
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    SSFTPsync.sshSftpDOWN(
                                            mPreValue[0],
                                            mPreValue[2],
                                            mPreValue[3],
                                            -1,
                                            mPreValue[1],
                                            fiLF.toString(),
                                            RemoteFileName
                                    );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                //mHandler.obtainMessage(STOP).sendToTarget();
                            }
                        }).start();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(MainActivity.this, "您选择的文件已同步到本地" + mPreValue[4] + "文件夹", Toast.LENGTH_SHORT).show();
                    }
                });


        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        // positive button logic
                      }
                });


        final AlertDialog alertDialog=builder.create();
        extra_alertDialog=alertDialog;

        // 向数据库中插入指定数据
        if(uri!=null)
        {
            SelectLANG(uri);
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mProgressBar=(ProgressBar)findViewById(R.id.google_progress);
        mProgressBar.setVisibility(View.INVISIBLE);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(intent, "请选择文件"), FILE_SELECT_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
                }
            }
        });
        fab_git= (FloatingActionButton) findViewById(R.id.fab_git);
        fab_git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mProgressBar.setVisibility(View.VISIBLE);
                alertDialog.show();
            }
        });
        fab_git.hide();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==FILE_SELECT_CODE)
        {
            if(resultCode==RESULT_OK)
            {
                Uri uri=data.getData();
                SelectLANG(uri);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        extra_alertDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent stsettings=new Intent(this,SettingsActivity.class);
            startActivity(stsettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_git) {

            fab.hide();
            fab_git.show();

        } else if (id == R.id.nav_external_storage) {
            fab_git.hide();
            fab.show();
        }else if(id==R.id.nav_contactsme)
        {
            String[] email = {"maxco292@hotmail.com"}; // 需要注意，email必须以数组形式传入
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822"); // 设置邮件格式
            intent.putExtra(Intent.EXTRA_EMAIL, email); // 接收人
            intent.putExtra(Intent.EXTRA_SUBJECT, "EditSharp反馈"); // 主题
            startActivity(Intent.createChooser(intent, "请选择邮件类应用"));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void SelectLANG(Uri uri)
    {
        DatabaseAdapter.getIntance(this).deleteAll();
        Intent i=new Intent(this,Editor.class);
        i.putExtra("filedata",uri.getPath());
        String filename=uri.getLastPathSegment();
        int pos=filename.lastIndexOf('.');
        if (pos>0)
        {
            String type=filename.subSequence(pos+1,filename.length()).toString();
            i.putExtra("type",type);
            switch(type)
            {
                case "py":
                    Toast.makeText(MainActivity.this,"python文件",Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseAdapter.getIntance(MainActivity.this).inserInfo(getListArray(PY3_LITERAL_NAMES));
                        }
                    }).start();
                    startActivity(i);
                    break;
                case "cpp":
                    Toast.makeText(MainActivity.this,"c++文件",Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseAdapter.getIntance(MainActivity.this).inserInfo(getListArray(CPP_LITERAL_NAMES));
                        }
                    }).start();
                    startActivity(i);
                    break;
                case "h":
                    Toast.makeText(MainActivity.this,"C++/C headers文件",Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseAdapter.getIntance(MainActivity.this).inserInfo(getListArray(CPP_LITERAL_NAMES));
                        }
                    }).start();
                    startActivity(i);
                    break;
                case "cs":
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseAdapter.getIntance(MainActivity.this).inserInfo(getListArray(CS_LITERAL_NAMES));
                        }
                    }).start();
                    Toast.makeText(MainActivity.this,"CSHARP文件",Toast.LENGTH_SHORT).show();
                    startActivity(i);
                    break;
                case "lua":
                    Toast.makeText(MainActivity.this,"lua文件",Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseAdapter.getIntance(MainActivity.this).inserInfo(getListArray(LUA_LITERAL_NAMES));
                        }
                    }).start();
                    startActivity(i);
                    break;
                case "java":
                    Toast.makeText(MainActivity.this,"java文件",Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseAdapter.getIntance(MainActivity.this).inserInfo(getListArray(JAVA_LITERAL_NAMES));
                        }
                    }).start();
                    startActivity(i);
                    break;
                case "c":
                    Toast.makeText(MainActivity.this,"c文件",Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseAdapter.getIntance(MainActivity.this).inserInfo(getListArray(CPP_LITERAL_NAMES));
                        }
                    }).start();
                    startActivity(i);
                    break;
                default:
                    Toast.makeText(MainActivity.this,"请选择正确的代码文件",Toast.LENGTH_SHORT).show();
                    break;
            }
        }else
        {
            Toast.makeText(MainActivity.this,"请选择正确的代码文件",Toast.LENGTH_SHORT).show();
        }
    }
}
