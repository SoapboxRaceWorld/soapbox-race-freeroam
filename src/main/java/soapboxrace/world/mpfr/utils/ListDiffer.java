package soapboxrace.world.mpfr.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListDiffer
{
    public static void main(String[] args)
    {
        List<String> initial = new ArrayList<String>()
        {
            {
                add("FLAMINGPEACOCK");
                add("NFSDEVELLIS");
                add("NFSDREW");
                add("NFSNULL");
                add("KEULE306");
                add("berkay2578");
                add("NILZAO");
                add("LEORBLX");
                add("NICOSPEED");
                add("METONATOR");
                add("SPEEDYHEART");
                add("DANKMEMER");
            }
        };

        List<String> newList = new ArrayList<String>()
        {
            {
                add("FLAMINGPEACOCK");
                add("NFSDEVELLIS");
                add("NFSDREW");
                add("NFSNULL");
                add("SPEEDYHEART");
                add("TRAPNATION");
                add("MASADADRM");
                add("OKHAND");
            }
        };

        ListDifferences<String> differences = getDifferences(
                initial,
                newList
        );

        System.out.println("Original list: " + initial);
        System.out.println("New list: " + newList);
        System.out.println("------------- differences -------------");
        System.out.println("Retained: " + differences.getKept());
        System.out.println("Added: " + differences.getAdded());
        System.out.println("Removed: " + differences.getRemoved());
    }


    public static class ListDifferences<T>
    {
        private final Collection<T> kept;
        private final Collection<T> added;
        private final Collection<T> removed;

        ListDifferences(Collection<T> kept, Collection<T> added, Collection<T> removed)
        {
            this.kept = kept;
            this.added = added;
            this.removed = removed;
        }

        public Collection<T> getAdded()
        {
            return added;
        }

        public Collection<T> getKept()
        {
            return kept;
        }

        public Collection<T> getRemoved()
        {
            return removed;
        }

        @Override
        public String toString()
        {
            return String.format("ListDifferences{added=%s/kept=%s/removed=%s}", this.getAdded(), this.getKept(), this.getRemoved());
        }
    }

    public static <T> ListDifferences<T> getDifferences(Collection<T> first, Collection<T> second)
    {
        List<T> clonedFirst = clone(first);
        List<T> clonedSecond = clone(second);

        List<T> alreadyThere = clone(clonedSecond);
        alreadyThere.retainAll(clonedFirst);
        List<T> added = clone(clonedSecond);
        added.removeAll(alreadyThere);
        List<T> removed = clone(clonedFirst);
        removed.removeAll(alreadyThere);

        return new ListDifferences<>(alreadyThere, added, removed);
    }

    private static <T> List<T> clone(Collection<T> collection)
    {
        ArrayList<T> list = new ArrayList<>();
        list.addAll(collection);

        return list;
    }
}
