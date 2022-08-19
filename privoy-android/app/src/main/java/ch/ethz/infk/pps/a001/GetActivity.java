package ch.ethz.infk.pps.a001;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class GetActivity extends AppCompatActivity {

    String firstTop;
    String secondTop;
    String thirdTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get);
        getTopic1();
        getTopic2();
        getTopic3();

    }
    public void getTopic1() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = "getendpoint";
        StringRequest objectRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("Get Response", response);
                        firstTop = new String(response);
                        TextView getTV1 = (TextView) findViewById(R.id.tvTopic1);
                        getTV1.setText(firstTop);
                        getTV1.setMovementMethod(new ScrollingMovementMethod());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Get Error Response", error.toString());
                    }


                });
        requestQueue.add(objectRequest);

    }

    public void getTopic2() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = "getendpoint";
        StringRequest objectRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("Get Response", response);
                        secondTop = new String(response);
                        TextView getTV2 = (TextView) findViewById(R.id.tvTopic2);
                        getTV2.setText(secondTop);
                        getTV2.setMovementMethod(new ScrollingMovementMethod());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Get Error Response", error.toString());
                    }


                });
        requestQueue.add(objectRequest);

    }

    public void getTopic3() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = "getendpoint";
        StringRequest objectRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("Get Response", response);
                        thirdTop = new String(response);
                        TextView getTV3 = (TextView) findViewById(R.id.tvTopic3);
                        getTV3.setText(thirdTop);
                        getTV3.setMovementMethod(new ScrollingMovementMethod());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Get Error Response", error.toString());
                    }


                });
        requestQueue.add(objectRequest);

    }

}