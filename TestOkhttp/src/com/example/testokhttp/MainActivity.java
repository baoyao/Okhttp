package com.example.testokhttp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/***
 * 本文使用的是老版本的okhttp2.7.5
 * 本文出自： http://blog.csdn.net/lmj623565791/article/details/47911083
 * 
 * okhttp官方地址（已经更新到3.x）： https://github.com/square/okhttp
 * 
 * 封装okhttp的demo: https://github.com/hongyangAndroid/okhttp-utils
 * 
 * okhttp2.7.5.jar下载地址：(已过时)
 * https://search.maven.org/remote_content?g=com.squareup.okhttp&a=okhttp
 * &v=LATEST
 * 
 * okhttp依赖的okio包下载地址：(已过时)http://square.github.io/okhttp/
 * https://search.maven.org/remote_content?g=com.squareup.okio&a=okio&v=LATEST
 * 
 * @author houen.bao
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		requestGet("https://github.com/hongyangAndroid");
		requestPost("https://github.com/hongyangAndroid");
	}

	private void requestGet(String url) {
		OkHttpClient mOkHttpClient = new OkHttpClient();
		final Request request = new Request.Builder().url(url).build();
		Call call = mOkHttpClient.newCall(request);
		call.enqueue(new Callback() {

			@Override
			public void onFailure(Request arg0, IOException arg1) {
				arg1.printStackTrace();
			}

			@Override
			public void onResponse(Response arg0) throws IOException{
				Log.v("tt", "onResponse: " + arg0.body().string());
			}
		});

	}

	private void requestPost(String url) {
		OkHttpClient mOkHttpClient = new OkHttpClient();
		FormEncodingBuilder builder = new FormEncodingBuilder();
		builder.add("username", "jack");
		Request request = new Request.Builder().url(url).post(builder.build())
				.build();
		mOkHttpClient.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Request paramRequest,
					IOException paramIOException) {
				// TODO Auto-generated method stub
				paramIOException.printStackTrace();
			}

			@Override
			public void onResponse(Response paramResponse) throws IOException {
				// TODO Auto-generated method stub
				Log.v("tt", "onResponse: " + paramResponse.body().string());
			}
		});
	}

	// 以下是封装的OkHttpClientManager的调用方法
	/**
	 * 一般的get请求<br>
	 * 对于一般的请求，我们希望给个url，然后CallBack里面直接操作控件。
	 */
	public void test1(final TextView mTv) {
		OkHttpClientManager.getAsyn("https://www.baidu.com",
				new OkHttpClientManager.ResultCallback<String>() {
					@Override
					public void onError(Request request, Exception e) {
						e.printStackTrace();
					}

					@Override
					public void onResponse(String u) {
						mTv.setText(u);// 注意这里是UI线程
					}
				});
	}

	/**
	 * 文件上传且携带参数<br>
	 * 我们希望提供一个方法，传入url,params,file,callback即可。<br>
	 * 键值对没什么说的，参数3为file，参数4为file对应的name，这个name不是文件的名字； <br>
	 * 对应于http中的<input type="file" name="mFile" ><br>
	 * 对应的是name后面的值，即mFile.<br>
	 * 
	 * @throws IOException
	 */
	public void test2(File file) throws IOException {
		OkHttpClientManager.postAsyn(
				"http://192.168.1.103:8080/okHttpServer/fileUpload",//
				new OkHttpClientManager.ResultCallback<String>() {
					@Override
					public void onResponse(String result) {
						
					}

					@Override
					public void onError(Request request, Exception e) {
						// TODO Auto-generated method stub
						e.printStackTrace();
					}
				},//
				file,//
				"mFile",//
				new OkHttpClientManager.Param[] {
						new OkHttpClientManager.Param("username", "zhy"),
						new OkHttpClientManager.Param("password", "123") });
	}

	/**
	 * 文件下载<br>
	 * 对于文件下载，提供url，目标dir，callback即可。
	 */
	public void test3() {
		OkHttpClientManager
				.downloadAsyn(
						"http://192.168.1.103:8080/okHttpServer/files/messenger_01.png",
						Environment.getExternalStorageDirectory()
								.getAbsolutePath(),
						new OkHttpClientManager.ResultCallback<String>() {
							@Override
							public void onError(Request request, Exception e) {
								e.printStackTrace();
							}

							@Override
							public void onResponse(String response) {
								// 文件下载成功，这里回调的reponse为文件的absolutePath
							}
						});
	}

	/**
	 * 展示图片<br>
	 * 展示图片，我们希望提供一个url和一个imageview，如果下载成功，直接帮我们设置上即可。<br>
	 * 内部会自动根据imageview的大小自动对图片进行合适的压缩。虽然，这里可能不适合一次性加载大量图片的场景，但是对于app中偶尔有几个图片的加载
	 * ，还是可用的。
	 */
	public void test4(ImageView mImageView) {
		OkHttpClientManager.displayImage(mImageView,
				"http://images.csdn.net/20150817/1.jpg");
	}

	/**
	 * 整合Gson<br>
	 * 很多人提出项目中使用时，服务端返回的是Json字符串，希望客户端回调可以直接拿到对象，于是整合进入了Gson，完善该功能。<br>
	 * （一）直接回调对象<br>
	 * 服务端返回：{"username":"zhy","password":"123"}<br>
	 * 我们传入泛型User，在onResponse里面直接回调User对象。 <br>
	 * 这里特别要注意的事，如果在json字符串->实体对象过程中发生错误，程序不会崩溃，onError方法会被回调。<br>
	 * 注意：这里做了少许的更新，接口命名从StringCallback修改为ResultCallback。
	 * 接口中的onFailure方法修改为onError。<br>
	 */
	public void test5(final TextView mTv) {
		OkHttpClientManager.getAsyn(
				"http://192.168.56.1:8080/okHttpServer/user!getUser",
				new OkHttpClientManager.ResultCallback<User>() {
					@Override
					public void onError(Request request, Exception e) {
						e.printStackTrace();
					}

					@Override
					public void onResponse(User user) {
						mTv.setText(user.toString());// UI线程
					}
				});
	}

	/**
	 * (二) 回调对象集合<br>
	 * 依然是上述的User类，服务端返回<br>
	 * [{"username":"zhy","password":"123"},{"username":"lmj","password":"12345"
	 * }]<br>
	 * 唯一的区别，就是泛型变为List &lt User &gt <br>
	 * 则客户端可以如下调用：
	 */
	public void test6(final TextView mTv) {
		OkHttpClientManager.getAsyn(
				"http://192.168.56.1:8080/okHttpServer/user!getUsers",
				new OkHttpClientManager.ResultCallback<List<User>>() {
					@Override
					public void onError(Request request, Exception e) {
						e.printStackTrace();
					}

					@Override
					public void onResponse(List<User> us) {
						Log.e("TAG", us.size() + "");
						mTv.setText(us.get(1).toString());
					}
				});
	}

}
