/*
       Copyright 2025 Kyndryl Corp, All Rights Reserved

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.kyndryl.cjot.stocktrader.myapp.json;


import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/** JSON-B POJO class representing an MyApp JSON object and an JNoSQL Entity*/
@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class MyAppEntity {
    @Id() private String id;
    @Column private String owner;
    @Column private String loyalty;
    @Column private double balance;
    @Column private double commissions;
    @Column private int free;
    @Column private String sentiment;
    @Column private double nextCommission;
    @Column private String operation;

    public MyAppEntity() { //default constructor
        setId(UUID.randomUUID().toString());
        setOwner("Someone Unknown");
        setLoyalty("Basic");
        setBalance(50.0);
        setCommissions(0.0);
        setFree(0);
        setSentiment("Unknown");
        setNextCommission(9.99);
    }

    public MyAppEntity(String initialOwner) { //primary key constructor
        this();
        setOwner(initialOwner);
    }

    public MyAppEntity(String initialOwner, String initialLoyalty, double initialBalance, double initialCommissions,
                     int initialFree, String initialSentiment, double initialNextCommission) {
        setId(UUID.randomUUID().toString());
        setOwner(initialOwner);
        setLoyalty(initialLoyalty);
        setBalance(initialBalance);
        setCommissions(initialCommissions);
        setFree(initialFree);
        setSentiment(initialSentiment);
        setNextCommission(initialNextCommission);
    }
    public MyAppEntity(String id, String initialOwner, String initialLoyalty, double initialBalance, double initialCommissions,
                   int initialFree, String initialSentiment, double initialNextCommission) {
        setId(id);
        setOwner(initialOwner);
        setLoyalty(initialLoyalty);
        setBalance(initialBalance);
        setCommissions(initialCommissions);
        setFree(initialFree);
        setSentiment(initialSentiment);
        setNextCommission(initialNextCommission);
    }

}
