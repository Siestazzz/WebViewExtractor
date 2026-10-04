# IWebSetting 深层API映射（source-first）

8方法逐条核实，未读能力报告。原事实保留canonical-facts-wrapper-api-v1.jsonl与主agent冻结generic-v3-development-facts.jsonl。当前canonical修订为默认H5 factory正常路径明确core1对应smtt.WebSettings API，并保留source_call_api=IWebSetting。没有按方法同名修改匹配规则。

H5BaseView→h→WebCreator c()/fallback/预热/池→new CustomWebView(...,1)→getSettings core1 MttWebSetting(smttSettings)。Mtt adapter构造保存同一settings，8 setter都参数原值转发。两个FileURLs方法均SDK>=16才转发，其余无版本门。

smtt.WebSettings又分真正IX5WebSettings与Android fallback：f12712c true/non-null走IX5；false/native settings非null走Android。6个直接调用、2个FileURLs通过反射j.a(webSettings, literal methodName, Boolean.TYPE, Boolean.valueOf(z2))。不能将8方法无条件Android-only，更不能拿IWebSetting同名当alias。generic SysWebSetting路径虽存在，却未证明默认审计H5 factory选它，不新增无条件Sys canonical。

JSONL为每方法保存caller值、factory链、adapter字段/构造、完整setter与smtt底层方法、分支条件、具体terminal API及hash。当前评分器不读映射或忽略未知底层时应保持未匹配，不能声称这次oracle源证规范化是生产召回提升。
