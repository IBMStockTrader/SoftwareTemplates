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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;

import lombok.*;

/** JSON-B POJO class representing a request or response to some backend */

//TODO Add additional fields or methods as needed
//TODO Rename this to what you're actually using
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table
@NamedQuery(name = "EntityTwo.findByOwner", query = "SELECT s FROM EntityTwo s WHERE s.portfolio.owner = :owner")
@NamedQuery(name = "EntityTwo.findByOwnerAndSymbol",
        query = "SELECT s FROM EntityTwo s WHERE s.portfolio.owner = :owner AND s.symbol = :symbol")
/** JSON-B POJO class representing a EntityTwo JSON object */
public class EntityTwo {

    @Id
    @Column(nullable = false, length = 8)
    private String symbol;
    private int shares;
    private double commission;
    private double price;
    private double total;

    @Column(name="dateQuoted")
    private String date;

    @Id
    @ManyToOne
    @JoinColumn(name = "owner")
    private EntityOne entityOne;

}