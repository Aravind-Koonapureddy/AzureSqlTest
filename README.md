Azure SQL Support money and small money. The Range is money -922,337,203,685,477.5808 to 922,337,203,685,477.5807 Small Money 214,748.3648 to 214,748.3647. 
When we try to insert the value beyond the range, it inserting wrong values.

When I try to insert the value beyond the range, application is not failing instead its inserting like below. 

|I try to Update|-214758.3648|922337203685487.5808| |Bulk Load Updated to|-214738.3649|922337203685467.5808| | | | |
|I try to Update|214758.3647|922337203685487.5807| |Bulk Load Updated to|214738.3648|-922337203685467.5809| | | | |
|I try to Update|-214728.3649|922337203685457.5808| |Bulk Load Updated to|214768.3647|922337203685497.5807| | | | |
|I try to Update|-214778.3648|-922337203685457.5809| |Bulk Load Updated to|-214718.3649|-922337203685497.5808| | | | | |
I try to Update|214778.3647|-922337203685597.5808| |Bulk Load Updated to|214718.3648|-922337203685357.5809|

When attempting to insert values beyond the allowed range, the operation results in the insertion of inaccurate values. 
here when I perform batch insert operations, and it functions as intended. However, during bulk load operations using SQLServerBulkCopy, 
it inserts invalid values instead of failing. Upon debugging the mssql-jdbc jar code, I identified that this issue occurs internally within the convertMoneyToBytes() 
method of the DDC class in the com.microsoft.sqlserver.jdbc package. This method converts the provided values to the unscaled value of BigInteger and then to int or long values. 
Unfortunately, this process leads to complications when the given value surpasses the range of the corresponding int or long data type, resulting in the insertion of incorrect values. 

 
