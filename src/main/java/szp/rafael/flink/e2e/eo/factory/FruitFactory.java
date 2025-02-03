package szp.rafael.flink.e2e.eo.factory;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.javafaker.Faker;
import szp.rafael.flink.e2e.eo.dto.Fruit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;

public class FruitFactory {
    public static Fruit createFruit() {
        Faker faker = new Faker();
        String fruitName = faker.food().fruit();
        var fruit = new Fruit();
        fruit.setName(fruitName);
        BigDecimal weight = new BigDecimal(new SecureRandom().nextDouble(0.1, 10.0)).setScale(2, RoundingMode.HALF_EVEN);
        fruit.setWeight(weight.doubleValue());
        return fruit;
    }

    public static String createJsonFruit(){
        var fruit = createFruit();
        return JSON.toJSONString(fruit);
    }
}
