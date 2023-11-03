package com.jerry.pilipala.infrastructure.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PartitionEnum {
    private String partition;
    private String subPartition;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static List<PartitionEnum> partitionList = null;


    public static List<PartitionEnum> partitions() {
        if (Objects.isNull(partitionList)) {
            synchronized (PartitionEnum.class) {
                if (Objects.isNull(partitionList)) {
                    partitionList = new ArrayList<>();
                    try {
                        ClassPathResource classPathResource = new ClassPathResource("partition.json");
                        InputStream partitionInputStream = classPathResource.getInputStream();
                        byte[] bytes = partitionInputStream.readAllBytes();
                        Map map = mapper.readValue(bytes, Map.class);
                        List list = (List) map.get("partitions");
                        list.forEach(p -> partitionList.add(mapper.convertValue(p, PartitionEnum.class)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return partitionList;
    }

}
