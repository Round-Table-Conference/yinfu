数据层源代码

本文件夹包含Android应用的数据访问层代码，包括API服务、适配器、本地存储和模型类。

子文件夹说明：
- adapter/      - RecyclerView适配器（聊天、紧急联系人、用药提醒）
- api/          - Retrofit网络请求服务
- local/        - 本地存储（聊天记录）
- model/        - 数据模型类

主要文件说明：
- ApiService.kt          - API接口定义
- RetrofitClient.kt     - Retrofit客户端配置
- SessionManager.kt     - 会话管理（Token存储）
- ChatHistoryManager.kt - 聊天记录本地存储管理
- EmergencyContactAdapter.kt - 紧急联系人列表适配器
- MedicationAdapter.kt  - 用药提醒适配器
- ChatAdapter.kt        - 聊天消息适配器
