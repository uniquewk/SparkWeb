## Overview

JavaBean 属性验证器，用于JavaBean属性定义注解规则进行验证。

## Usage

```java
public class Order
{
    @Required(message="订单编号不能为空")
	@Pattern(regexp="^([0-9]{6}\\w+)$", message="订单编号格式不正确")
	private String orderId;
	
	@Range(max = 10000, min = 150, message = "订单价格必须在{min}~{max}元之间")
	private double price;
	
	@Required(message="订单必须含有订单项")
	private OrderItem[] items;
	
	@Required
	@Email
	private String email;
	
	@AssertTrue
	private boolean finished;
	
	public Order(){}
	
	// setter, getter...
}


Order order = new Order();
order.setOrderId("abc");
order.setPrice(110.56);
order.setEmail("abc@gmail.com");

// Valildate Order
ConstraintViolation<Order>[] violations = Validator.validate(order);
if(violations != null && violations.length > 0)
{
	for(ConstraintViolation<Order> cv : violations)
	{
		System.out.println(String.format("Field: %s invalid(%s), because %s", cv.getFieldName(), cv.getInvalidValue(), cv.getMessage()));
	}
	
}

```

## 自定义验证规则


