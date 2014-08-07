gdx-http
========

###Notice

This extension is still a work-in-progress since it is - somewhat ironically - not GWT-compatible.  In the meantime, please let me know if you have any questions or suggestions, I'd be glad to hear them!


## HttpClient

The HttpClient interface serves as a wrapper for LibGDX's Net#sendHttpRequest() method, automatically attaching your headers (including cookies, discussed below) to all requests sent through it and intercepting responses so it can handle any return headers before passing it off to the listener you've supplied.  For most use cases, this is the only class you'll deal with directly.

There is currently only one implementation, DefaultHttpClient.  

### Setting Up HttpClient


```
public class MyGameClass extends ApplicationListener {
HttpClient client;

public void create() {
   client = new DefaultHttpClient();
  }
}

```
DefaultHttpClient creates an instance of CookieManager on creation, then loads any cookies stored from previous sessions.  

### Using HttpClient

To set headers use the setHeader() method:

```
   client.setHeader("User-Agent", "MyGame Client");
   // ...or...
   client.setHeader(HttpClient.HeaderFields.USER_AGENT, "MyGame Client");
   
   
```

Once you've assigned a header field and value, that header will be included on every subsequent request sent via the HttpClient instance.  You can clear a header you've previously assigned, preventing it from being passed to the server, by setting it to null.

```
   client.setHeader(HttpClient.HeaderFields.USER_AGENT, null);
```


To send an HTTP request, proceed as you would with Net#sendHttpRequest():

```
HttpRequest request = new HttpRequest(HttpMethods.POST);
request.setUrl("http://www.example.com/web_service/");
httpClient.sendHttpRequest (request, myHttpResponseListener);
```

HttpClient will process any response headers (like Set-Cookie) before calling your listener.



## CookieManager

CookieManager makes up the bulk of this extension.  When using DefaultHttpClient it is created automatically.  Cookies are automatically handled behind the scenes, but if you need to view or control cookies, you can use the CookieManager.

You can get the instance by calling the HttpClient's getCookieManager() method.

```
cookieMgr = client.getCookieManager();

```

When a CookieManager is associated with a HttpClient (as it is by default), every outgoing HTTP request will include the relevant cookies, and every HTTP response will automatically update the CookieManager with any new or updated cookies. note that "relevant cookies" means that each cookie's given domain, path and expiration date will be taken into account when attaching them to request headers, parsing the URL to determine whether a cookie should be sent or not.  Cookies without expirations ("session cookies") will not be stored in Preferences and will only last until the app closes.  You can override this by giving the cookie an expiration date, as described below.

### Using CookieManager



#### Viewing a preset cookie
```
Cookie cookie = cookieMgr.getCookie("my_cookie_name");
    
    /* or, if you just want the "value" field: */
String value = cookieMgr.getCookieValue("my_cookie_name");
```

#### editing a cookie

```
Cookie cookie = cookieMgr.getCookie("my_cookie_name");
cookie.path = "/";
cookie.value = "new_value";
```

#### Creating a new cookie
```
Cookie newCookie = Cookie.obtain("cookie_name", "cookie_value");
cookieMgr.addCookie(newCookie);
```

Note that CookieManager#addCookie() will override any cookie with the same name (case sensitive).

CookieManager will automatically save all cookies when receiving http responses, but if alter or add cookies by hand, you may want to call CookieManager#save() to persist the changes.  If left unsaved, the cookies will be reverted to their previous state once you restart the application.







