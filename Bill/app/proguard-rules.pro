# 保留应用类
-keep class com.example.bill.** { *; }

# 保留 Application 子类
-keep class **.MyApplication extends android.app.Application { *; }

# 保留 ViewBinding
-keep class **.databinding.*Binding { *; }

# 保留 Gson 序列化字段
-keep class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 保留 R 类（资源引用）
-keep class **.R$* { *; }

# Kotlin 相关
-keep class kotlin.** { *; }
-dontwarn kotlin.**

# 避免 lambda 报错
-dontwarn java.lang.invoke.*
