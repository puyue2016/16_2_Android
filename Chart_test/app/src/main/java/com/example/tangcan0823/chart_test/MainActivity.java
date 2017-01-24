package com.example.tangcan0823.chart_test;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.PermissionChecker;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;


public class
MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private int MY_PERMISSION_REQUEST_MULTI = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);


        //ストレージへの権限の有無
        if(PermissionChecker.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            new AlertDialog.Builder(this)
                    .setTitle("アプリケーション権限について")
                    .setMessage("ストレージへのアクセス権限を許可してください" + "\n")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //パーミッション許可取得
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST_MULTI);
                        }
                    })
                    .create()
                    .show();
        }




        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        setupDrawerContent(mNavigationView);
        if(PermissionChecker.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            new AlertDialog.Builder(this)
                    .setTitle("アプリケーション権限について")
                    .setMessage("ストレージへのアクセス権限を許可してください" + "\n")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//パーミッション許可取得
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST_MULTI);
                        }
                    })
                    .create()
                    .show();
        }
        setUpProfileImage();
        switchToPiechart();

            }

    private void switchTohitori() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new HitoriFragment()).commit();
        mToolbar.setTitle(R.string.navigation_hirori);
    }
    private void switchTotomo() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new TomoFragment()).commit();
        mToolbar.setTitle(R.string.navigation_tomo);
    }

    private void switchToData() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new DataFragment()).commit();
        mToolbar.setTitle(R.string.navigation_data);
    }

    private void switchToPiechart() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new PiechartFragment()).commit();
        mToolbar.setTitle(R.string.navigation_piechart);
    }



    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {

                            case R.id.navigation_item_hitori:
                                switchTohitori();
                                break;

                            case R.id.navigation_item_tomo:
                                switchTotomo();
                                break;

                            case R.id.navigation_item_data:
                                switchToData();
                                break;

                            case R.id.navigation_item_piechart:
                                switchToPiechart();
                                break;


                        }
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void setUpProfileImage() {
        View headerView = mNavigationView.inflateHeaderView(R.layout.navigation_header);
        View profileView = headerView.findViewById(R.id.profile_image);
        if (profileView != null) {
            profileView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchToPiechart();
                    mToolbar.setTitle(R.string.app_name);
                    mDrawerLayout.closeDrawers();
                }
            });
        }

    }


}