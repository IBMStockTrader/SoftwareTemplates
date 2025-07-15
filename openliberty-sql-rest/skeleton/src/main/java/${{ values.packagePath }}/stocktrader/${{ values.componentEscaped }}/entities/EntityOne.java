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

package ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Id;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.json.JsonObject;
import jakarta.json.bind.annotation.JsonbTransient;

import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

//TODO Add additional fields or methods as needed
//TODO Rename this to what you're actually using
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table
@NamedQuery(name = "EntityOne.findAll", query = "SELECT p FROM EntityOne p ORDER BY p.owner ASC")
/** JSON-B POJO class representing a EntityOne JSON object */
public class EntityOne {

    @Id
    @Column(nullable = false, length = 32)
    private String owner;

    private double total;

    @Transient
    private double lastTrade; //used to communicate total cost of the current trade to the CashAccount microservice

    @Column(nullable = true, length = 64)
    private String accountID;

    @Transient
    private String operation;

    @Transient
    private JsonObject entityTwos;

    @JsonbTransient
    @OneToMany(mappedBy = "entityOne", cascade = CascadeType.ALL)
    private Set<EntityTwo> entityTwoList = new LinkedHashSet<>();

    public void addEntityTwo(EntityTwo entityTwo) {
        // TODO verify this is the logic you want
        if (entityTwo != null) {
            String symbol = entityTwo.getSymbol();
            if (symbol != null) {
                var entity = entityTwoList.stream()
                        .filter(e ->e.getSymbol().equalsIgnoreCase(symbol))
                        .findFirst().get();
                entityTwoList.remove(entity);
                entityTwoList.add(entityTwo);
            }
        }
    }

}
