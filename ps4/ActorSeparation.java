/**class to hold actor name and degrees of seperation in single object for easy access
 * Feb 2020
 * @author Tal Sternberg and Jason Saber
 * @param <V>
 * @param <E>
 */
class ActorSeparation <V, E> {
    //declare instance vars
    public V name;
    public E separation;

    public ActorSeparation(V n, E s){
        //set vars
        name = n;
        separation = s;
    }

    @Override
    public String toString() {
        return name + " : " + separation;
    }
}

