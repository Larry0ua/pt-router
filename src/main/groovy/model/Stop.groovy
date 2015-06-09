package model
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeSuper = true)
@EqualsAndHashCode
class Stop extends Point {
    String name
    String id
}
