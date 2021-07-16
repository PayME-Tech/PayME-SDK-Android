PayME SDK là bộ thư viện để các app có thể tương tác với PayME Platform. PayME SDK bao gồm các chức năng chính như sau:

- Hệ thống đăng nhập, eKYC thông qua tài khoản ví PayME
- Hỗ trợ app lấy thông tin số dư ví PayME
- Chức năng nạp rút từ ví PayME.

**Một số thuật ngữ**

|      | Name    | Giải thích                                                   |
| ---- | ------- | ------------------------------------------------------------ |
| 1    | app     | Là app mobile iOS/Android hoặc web sẽ tích hợp SDK vào để thực hiện chức năng thanh toán ví PayME. |
| 2    | SDK     | Là bộ công cụ hỗ trợ tích hợp ví PayME vào hệ thống app.     |
| 3    | backend | Là hệ thống tích hợp hỗ trợ cho app, server hoặc api hỗ trợ  |
| 4    | AES     | Hàm mã hóa dữ liệu AES256 PKCS5. [Tham khảo](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) |
| 5    | RSA     | Thuật toán mã hóa dữ liệu RSA.                               |
| 6    | IPN     | Instant Payment Notification , dùng để thông báo giữa hệ thống backend của app và backend của PayME |

**Bước 1 :**

Tích hợp thư viện hỗ trợ. Dung lượng 472.76 KB

**File build.gradle Project**

```kotlin
allprojects {
  repositories {
    google()
    jcenter()
   maven {
      url "https://plugins.gradle.org/m2/"
    }
  maven {
      url "https://jitpack.io"
  }
 }
}
```

- **File build.gradle Module**

```java
android {
    ...
    packagingOptions {
        exclude 'META-INF/DEPENDENCIES'
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/license.txt'
        exclude 'META-INF/NOTICE'
        exclude 'META-INF/NOTICE.txt'
        exclude 'META-INF/notice.txt'
        exclude 'META-INF/ASL2.0'
    }
    ...
}
dependencies {
...
  implementation 'com.github.PayME-Tech:PayME-SDK-Android:0.9.9'
...
}
```

- **Android Manifests**

Update như sau:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="com.example.app"
        xmlns:tools="http://schemas.android.com/tools">
<uses-sdk tools:overrideLibrary="com.google.zxing.client.android" />

<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_INTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_INTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

<application android:requestLegacyExternalStorage="true" >

```

Tham khảo https://developer.android.com/studio/build/manifest-merge nếu bị lỗi.

# Cách sử dụng SDK:

Hệ thống PayME sẽ cung cấp cho app tích hợp các thông tin sau:

- **PublicKey** : Dùng để mã hóa dữ liệu, app tích hợp cần truyền cho SDK để mã hóa.
- **AppToken** : AppId cấp riêng định danh cho mỗi app, cần truyền cho SDK để mã hóa
- **SecretKey** : Dùng đã mã hóa và xác thực dữ liệu ở hệ thống backend cho app tích hợp.

Bên App sẽ cung cấp cho hệ thống PayME các thông tin sau:

- **AppPublicKey** : Sẽ gửi qua hệ thống backend của PayME dùng để mã hóa.
- **AppPrivateKey**: Sẽ truyền vào PayME SDK để thực hiện việc giải mã.

Chuẩn mã hóa: RSA-512bit.

### Khởi tạo PayME SDK:

Trước khi sử dụng PayME SDK cần gọi phương thức khởi tạo một lần duy nhất để khởi tạo SDK.

```kotlin
import vn.payme.sdk.model.Env
val configColor = arrayOf<String>("#75255b","#9d455f"}
val payme: PayME = PayME(context, AppToken, PublicKey,ConnectToken, AppPrivateKey, configColor, LANGUAGES.VN, Env.SANDBOX, false)
// sau khi khởi tạo có thể gọi payme.login() tại đây
```

### login()

Có 2 trường hợp

- Dùng để login lần đầu tiên ngay sau khi khởi tạo PayME.
- Dùng khi accessToken hết hạn, khi gọi hàm của SDK mà trả về mã lỗi ERROR_CODE.EXPIRED, lúc này app cần gọi login lại để lấy accessToken dùng cho các chức năng khác.

Sau khi gọi login() thành công rồi thì mới gọi các chức năng khác của SDK ( openWallet, pay ... )

```kotlin
public fun login(
  onSuccess:(AccountStatus)->Unit,
  onError: (JSONObject?, Int, String?) -> Unit
}
```

Ví dụ:

```kotlin
public fun loginExample(){
  payme.loggin(		onSuccess = { accountStatus ->
                    if(accountStatus == AccountStatus.NOT_ACTIVATED){
                        //Tài khoản chưa kich hoạt
                    }
                    if(accountStatus == AccountStatus.NOT_KYC){
                        //Tài khoản chưa định danh
                    }
                    if(accountStatus == AccountStatus.KYC_APPROVED){
                        //Tài khoản đã định danh
                    }
                    }
                    walletView.setVisibility(View.VISIBLE)
               			},
                    onError = { jsonObject, code, message ->
                        PayME.showError(message)
                    })
}
```

configColor : là tham số màu để có thể thay đổi màu sắc giao dịch ví PayME, kiểu dữ liệu là chuỗi với định dạng #rrggbb. Nếu như truyền 2 màu thì giao diện PayME sẽ gradient theo 2 màu truyền vào.

[![img](https://github.com/PayME-Tech/PayME-SDK-Android-Example/raw/main/fe478f50-e3de-4c58-bd6d-9f77d46ce230.png?raw=true)](https://github.com/PayME-Tech/PayME-SDK-Android-Example/blob/main/fe478f50-e3de-4c58-bd6d-9f77d46ce230.png?raw=true)

Cách tạo **connectToken**:

connectToken cần để truyền gọi api từ tới PayME và sẽ được tạo từ hệ thống backend của app tích hợp. Cấu trúc như sau:

```kotlin
connectToken = AES256("{ timestamp: 34343242342, userId : "ABC", phone : "0909998877" }" + secretKey )
```

| **Tham số**    | **Bắt buộc** | **Giải thích**                                               |
| -------------- | ------------ | ------------------------------------------------------------ |
| **timestamp**  | Yes          | Thời gian tạo ra connectToken theo định dạng iSO 8601 , Dùng để xác định thời gian timeout cùa connectToken. Ví dụ 2021-01-20T06:53:07.621Z |
| **\*userId\*** | Yes          | là giá trị cố định duy nhất tương ứng với mỗi tài khoản khách hàng ở dịch vụ, thường giá trị này do server hệ thống được tích hợp cấp cho PayME SDK |
| **\*phone\***  | Yes           | Số điện thoại của hệ thống tích hợp, nếu hệ thống không dùng số điện thoại thì có thể không cần truyền lên hoặc truyền null |

Trong đó **\*AES\*** là hàm mã hóa theo thuật toán AES. Tùy vào ngôn ngữ ở server mà bên hệ thống dùng thư viện tương ứng. Xem thêm tại đây https://en.wikipedia.org/wiki/Advanced_Encryption_Standard

## Mã lỗi của PayME SDK

| **Hằng số**   | **Mã lỗi** | **Giải thích**                                               |
| :------------ | :----------- | :----------------------------------------------------------- |
| <code>EXPIRED</code> | <code>401</code>          | ***token*** hết hạn sử dụng |
| <code>NETWORK</code>  | <code>-1</code>          | Kết nối mạng bị sự cố |
| <code>SYSTEM</code>   | <code>-2</code>           | Lỗi hệ thống |
| <code>LIMIT</code>   | <code>-3</code>           | Lỗi số dư không đủ để thực hiện giao dịch |
| <code>ACCOUNT_NOT_ACTIVATED</code>   | <code>-4</code>           | Lỗi tài khoản chưa kích hoạt |
| <code>ACCOUNT_NOT_KYC</code>   | <code>-5</code>           | Lỗi tài khoản chưa định danh |
| <code>PAYMENT_ERROR</code>   | <code>-6</code>          | Thanh toán thất bại |
| <code>ERROR_KEY_ENCODE</code>   | <code>-7</code>           | Lỗi mã hóa/giải mã dữ liệu |
| <code>USER_CANCELLED</code>   | <code>-8</code>          | Người dùng thao tác hủy |
| <code>ACCOUNT_NOT_LOGIN</code>   | <code>-9</code>           | Lỗi chưa đăng nhập tài khoản |
| <code>BALANCE_ERROR</code>   | <code>-10</code>           | Lỗi khi thanh toán bằng ví PayME mà số dư trong ví không đủ |

## Các c**hức năng của PayME SDK**

### getAccountInfo()

App có thể dùng hàm này sau khi khởi tạo SDK để biết được trạng thái liên kết tới ví PayME.

```kotlin
public fun getAccountInfo(
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
        )
```

Ví dụ:

```kotlin
      payme?.getAccountInfo(
        onSuccess = { json: JSONObject? ->
        },
        onError = { jsonObject, code, message ->
                        PayME.showError(message)
                        if (code == ERROR_CODE.EXPIRED) {
                            walletView.setVisibility(View.GONE)
                            payme?.logout()
                        }
                    })
```

**openWallet() - Mở UI chức năng PayME tổng hợp**

```kotlin
public fun openWallet(onSuccess: (JSONObject)->Unit, onError:(JSONObject?, Int?, String) -> Unit )

```

Hàm này được gọi khi từ app tích hợp khi muốn gọi 1 chức năng PayME bằng cách truyền vào tham số Action như trên.

| **Tham số** | **Bắt buộc** | **Giải thích**                                               |
| :---------- | ------------ | ------------------------------------------------------------ |
| onSuccess   | Yes          | Dùng để bắt callback khi thực hiện giao dịch thành công từ PayME SDK |
| onError     | Yes          | Dùng để bắt callback khi có lỗi xảy ra trong quá trình gọi PayME SDK |

**Ví dụ :**

```kotlin
payme.openWallet(
	onSuccess = { json: JSONObject ->
              },
	onError = { jsonObject, code, message ->
			PayME.showError(message)
                        //Lỗi khi hết hạn đăng nhập
                        if (code == ERROR_CODE.EXPIRED) {
                            walletView.setVisibility(View.GONE)
                            payme.logout()
			    // hoặc có thể gọi payme.login() để tự động đăng nhập lại vì khi gọi login là đã có gọi payme.logout() trước.
			    // Sau đó có thể gọi lại openWallet() để mở lại UI ví PayME.
			}
		   }
		 )
}
```
### openKYC() -  Mở modal định danh tài khoản
Hàm này được gọi khi từ app tích hợp khi muốn mở modal định danh tài khoản ( yêu cầu tài khoản phải chưa định danh )

```kotlin
public fun openKYC(
  onSuccess: (JSONObject?) -> Unit,
  onError:(JSONObject?, Int?, String) -> Unit)
```
Ví dụ :

```kotlin
payme.openKYC(  
                onSuccess = { json: JSONObject ->
                },
                onError = { jsonObject, code, message ->
		}
	     )
```



### deposit() - Nạp tiền

```kotlin
public fun deposit(
  amount : Int,
  closeDepositResult: Boolean,
  onSuccess: (JSONObject) -> Unit,
  onError:(JSONObject?, Int, String?) -> Unit)
```
closeDepositResult : đóng lại màn hình sdk khi có kết quả nạp tiền thành công hoặc thất bại

Ví dụ :

```kotlin
payme.deposit(  
		amount,
		false,
                onSuccess = { json: JSONObject ->
                },
                onError = { jsonObject, code, message ->

		    ME.showError(message)
                    if (code == ERROR_CODE.EXPIRED) {
                        walletView.setVisibility(View.GONE)
                        payme.logout()
                    }
                    //ERROR_CODE.ACCOUNT_NOT_KYC : tài khoản chưa định danh
                    // ERROR_CODE.ACCOUNT_NOT_ACTIVETES : chưa kích hoạt
                    //cần gọi lại hàm openWallet

                    if (code == ERROR_CODE.ACCOUNT_NOT_KYC || code == ERROR_CODE.ACCOUNT_NOT_ACTIVETES) {
                        openWallet()
                    }
		}
	     )
```

Hàm này có ý nghĩa giống như khi gọi openWallet với action **Action.Deposit.**

### withdraw() - Rút tiền

```kotlin
public fun withdraw(
		    amount: Int,
		    closeWithdrawResult: Boolean,
                    onSuccess: (JSONObject) -> Unit,
		    onError: (JSONObject?, Int, String?) -> Unit)
```
closeWithdrawResult : đóng lại màn hình sdk khi có kết quả rút tiền thành công hoặc thất bại
		    

Ví dụ:

```kotlin
payme.withdraw( 
		amount,
		false,
                onSuccess = { json: JSONObject ->
                },
                onError = { jsonObject, code, message ->
                    PayME.showError(message)
                    if (code == ERROR_CODE.EXPIRED) {
                        walletView.setVisibility(View.GONE)
                        payme.logout()
                    }

                    if (code == ERROR_CODE.ACCOUNT_NOT_KYC || code == ERROR_CODE.ACCOUNT_NOT_ACTIVETES) {
                        openWallet()
                    }
                })
```

### transfer() - Chuyển tiền

```kotlin
public fun transfer(
		        amount: Int,
			description: String,
			closeTransferResult: Boolean,
			onSuccess: (JSONObject?) -> Unit,
			onError: (JSONObject?, Int, String?) -> Unit
		    )
```
amount: Số tiền cần chuyển

description : Ghi chú khi chuyển tiền

closeTransferResult : đóng lại màn hình sdk khi có kết quả chuyển tiền thành công hoặc thất bại
		    

Ví dụ:

```kotlin
payme.transfer( 
		amount,
		"chuyen tien cho ban nhe",
		false,
                onSuccess = { json: JSONObject ->
                },
                onError = { jsonObject, code, message ->
                    PayME.showError(message)
                    if (code == ERROR_CODE.EXPIRED) {
                        walletView.setVisibility(View.GONE)
                        payme.logout()
                    }

                    if (code == ERROR_CODE.ACCOUNT_NOT_KYC || code == ERROR_CODE.ACCOUNT_NOT_ACTIVETES) {
                        openWallet()
                    }
                })
```

### getSupportedServices()

App có thể dùng h này sau khi khởi tạo SDK để biết danh sách các dịch vụ mà PayME đang cung cấp



```kotlin
  public fun getSupportedServices(onSuccess: (ArrayList<Service>?) -> Unit,onError: (JSONObject?, Int?, String) -> Unit)  {
      
    }

```
### setLanguage()
Chuyển đổi ngôn ngữ của sdk
```kotlin
    fun setLanguage(context: Context,language: LANGUAGES){
    }
```



### openService()

Hàm này được gọi khi từ app tích hợp khi muốn gọi 1 dịch vụ mà  PayME cũng cấp bằng cách truyền vào tham số Service như trên

```kotlin
 public fun openService(
 	fragmentManager: FragmentManager,
        service: Service,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    )

```

Ví dụ:

```kotlin
  val service : Service = payme?.getListService()?.get(2)!!
  payme?.openService(
    this.supportFragmentManager,
    service, 
    onSuccess = { json: JSONObject? -> },
    onError = { jsonObject, code, message ->
                        PayME.showError(message)
                        if (code == ERROR_CODE.EXPIRED) {
                            walletView.setVisibility(View.GONE)
                            payme?.logout()
                        }
   })
```




### pay() - Thanh toán

Hàm này được dùng khi app cần thanh toán 1 khoản tiền từ ví PayME đã được kích hoạt.

```kotlin
public fun pay(
            fragmentManager: FragmentManager,
            infoPayment: InfoPayment,
  	    isShowResultUI: Boolean,
  	    payCode: String,
            onSuccess: ((JSONObject?) -> Unit)?,
            onError: ((JSONObject?, Int, String?) -> Unit)?,
        )
class InfoPayment {
    var action : String? = null
    var amount : Int? = null
    var note : String? = null
    var orderId : String? = null
    var storeId : Long 
    var type : String? = null
    var referExtraData : String? = null
}

- action: loại giao dịch ( 'PAYMENT' => thanh toán)
- amount: số tiền thanh toán
- note: Mô tả giao dịch từ phía đối tác
- orderId: mã giao dịch của đối tác, cần duy nhất trên mỗi giao dịch
- storeId: ID của store phía công thanh toán thực hiên giao dịch thanh toán
- type: OpenEWallet
- referExtraData: Thông tin bổ sung (extraData) là một nội dung được định nghĩa theo dạng chuỗi, chứa thông tin bổ sung của một giao dịch mà đối tác muốn nhận về khi hoàn tất một giao dịch với PAYME.
nếu Merchant ko cần IPN thêm data custom của mình có thể bỏ qua

-Khi thanh toán bằng ví PayME thì yêu cầu tài khoản đã kích hoạt,định danh và số dư trong ví phải lớn hơn số tiền thanh toán
thông tin tài khoản lấy qua hàm getAccountInfo()
thông tin số dư lấy qua hàm getWalletInfo()


```

Ví dụ:

```kotlin
val amount = convertInt(moneyPay.text.toString())
val nextValues = List(10) { Random.nextInt(0, 100000) }
val infoPayment = InfoPayment(
                    "PAY",
                    amount,
                    "Nội dung đơn hàng",
                    nextValues.toString(),
                    storeId,
                    "OpenEWallet",
                    ""
                )
payme?.pay( this.supportFragmentManager,
	    infoPayment,
	    true,
	    PAY_CODE.PAYME,
            onSuccess = { json: JSONObject? -> },
            onError = { jsonObject, code, message ->
                        if (message != null && message.length > 0) {
                            PayME.showError(message)
                        }
                        if (code == ERROR_CODE.EXPIRED) {
                            walletView.setVisibility(View.GONE)
                            payme?.logout()
                        }
                        if (code == ERROR_CODE.ACCOUNT_NOT_KYC || code == ERROR_CODE.ACCOUNT_NOT_ACTIVETES) {
                            openWallet()
                        }
                    }
            )


```
Danh sách PAY_CODE

| **Tham số**    | **Bắt buộc** | **Giải thích**                                               |
| -------------- | ------------ | ------------------------------------------------------------ |
| PAYME| Yes          | Thanh toán ví PayME |
| ATM | Yes          | Thanh toán thẻ ATM nội đia|
| CREDIT  | Yes           |  Thẻ tín dụng |
| MANUAL_BANK  | Yes           |  Thanh toán chuyển khoản ngân hàng |

### scanQR() - Mở chức năng quét mã QR để thanh toán

```kotlin
fun scanQR(
	fragmentManager: FragmentManager,
	payCode: String,
	onSuccess: (JSONObject?) -> Unit,
	onError: (JSONObject?, Int, String?) -> Unit
) : Unit 
```
Định dạng qr : 
```kotlin
 val qrString =  "{$type}|${storeId}|${action}|${amount}|${note}|${orderId}"
```

Ví dụ  : 
```kotlin
val qrString = "OPENEWALLET|54938607|PAYMENT|20000|Chuyentien|2445562323"
```

- action: loại giao dịch ( 'PAYMENT' => thanh toán)
- amount: số tiền thanh toán
- note: Mô tả giao dịch từ phía đối tác
- orderId: mã giao dịch của đối tác, cần duy nhất trên mỗi giao dịch
- storeId: ID của store phía công thanh toán thực hiên giao dịch thanh toán
- type: OPENEWALLET
- payCode : ở bảng payCode ở trên

### payQRCode() - thanh toán mã QR code
```kotlin
    fun payQRCode(
    fragmentManager: FragmentManager, 
    qr: String,
    payCode: String,
    isShowResultUI:Boolean,
    onSuccess: (JSONObject?) -> Unit,
    onError:(JSONObject?, Int, String?) -> Unit)
```

- qr: Mã QR để thanh toán  ( Định dạng QR như hàm scanQR() )
- isShowResultUI: Có muốn hiển thị kết quả giao dịch hay ko  
- payCode : ở bảng payCode ở trên


### getWalletInfo() - **Lấy các thông tin của ví**

```kotlin
public fun geWalletInfo(onSuccess: (JSONObject) -> Unit,onError:(JSONObject?, Int?, String) -> Unit)
```

- Trong trường hợp lỗi thì hàm sẽ trả về message mỗi tại hàm onError , khi đó app có thể hiển thị balance là 0.
- Trong trường hợp thành công SDK trả về thông tin như sau:

```kotlin
{
  "Wallet": {
    "balance": 111,
    "detail": {
      "cash": 1,
      "lockCash": 2
    }
  }
}
```
### close() - Đóng UI

Hàm này được dùng để app tích hợp đóng lại UI của SDK khi đang payment hoặc openWallet

```kotlin
 fun close(){
 }
```



**\*balance\*** : App tích hợp có thể sử dụng giá trị trong key balance để hiển thị, các field khác hiện tại chưa dùng.

**\*detail.cash :\*** Tiền có thể dùng

**\*detail.lockCash:\*** tiền bị lock
