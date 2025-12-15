-- Enable UUID extension (usually already enabled)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- FAVORITES TABLE
-- ============================================
-- Stores user's favorite recipes with snapshot data

CREATE TABLE IF NOT EXISTS public.favorites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    recipe_id TEXT NOT NULL,
    recipe_name TEXT NOT NULL,
    recipe_image_url TEXT NOT NULL DEFAULT '',
    recipe_category TEXT NOT NULL DEFAULT '',
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Prevent duplicate favorites for same user/recipe
    UNIQUE(user_id, recipe_id)
);

-- Index for faster queries by user
CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON public.favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_recipe_id ON public.favorites(recipe_id);

-- Enable RLS
ALTER TABLE public.favorites ENABLE ROW LEVEL SECURITY;

-- RLS Policies: Users can only see/modify their own favorites
CREATE POLICY "Users can view own favorites" 
    ON public.favorites FOR SELECT 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own favorites" 
    ON public.favorites FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own favorites" 
    ON public.favorites FOR DELETE 
    USING (auth.uid() = user_id);

-- ============================================
-- MEAL PLAN ENTRIES TABLE
-- ============================================
-- Stores meal plan entries with recipe references

CREATE TABLE IF NOT EXISTS public.meal_plan_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    recipe_id TEXT NOT NULL,
    recipe_name TEXT NOT NULL,
    recipe_image_url TEXT NOT NULL DEFAULT '',
    date DATE NOT NULL,
    meal_type TEXT NOT NULL CHECK (meal_type IN ('BREAKFAST', 'LUNCH', 'DINNER')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_meal_plan_user_id ON public.meal_plan_entries(user_id);
CREATE INDEX IF NOT EXISTS idx_meal_plan_date ON public.meal_plan_entries(date);
CREATE INDEX IF NOT EXISTS idx_meal_plan_user_date ON public.meal_plan_entries(user_id, date);

-- Enable RLS
ALTER TABLE public.meal_plan_entries ENABLE ROW LEVEL SECURITY;

-- RLS Policies: Users can only see/modify their own meal plan entries
CREATE POLICY "Users can view own meal plan" 
    ON public.meal_plan_entries FOR SELECT 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own meal plan entries" 
    ON public.meal_plan_entries FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own meal plan entries" 
    ON public.meal_plan_entries FOR UPDATE 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own meal plan entries" 
    ON public.meal_plan_entries FOR DELETE 
    USING (auth.uid() = user_id);

-- ============================================
-- SHOPPING LIST ITEMS TABLE
-- ============================================
-- Stores shopping list items with ingredient details

CREATE TABLE IF NOT EXISTS public.shopping_list_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    ingredient_name TEXT NOT NULL,
    quantity TEXT NOT NULL DEFAULT '',
    category TEXT NOT NULL DEFAULT 'OTHER',
    is_checked BOOLEAN NOT NULL DEFAULT FALSE,
    recipe_ids TEXT[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_shopping_list_user_id ON public.shopping_list_items(user_id);
CREATE INDEX IF NOT EXISTS idx_shopping_list_category ON public.shopping_list_items(category);

-- Enable RLS
ALTER TABLE public.shopping_list_items ENABLE ROW LEVEL SECURITY;

-- RLS Policies: Users can only see/modify their own shopping list
CREATE POLICY "Users can view own shopping list" 
    ON public.shopping_list_items FOR SELECT 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own shopping list items" 
    ON public.shopping_list_items FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own shopping list items" 
    ON public.shopping_list_items FOR UPDATE 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own shopping list items" 
    ON public.shopping_list_items FOR DELETE 
    USING (auth.uid() = user_id);

-- ============================================
-- PROFILES TABLE (Optional)
-- ============================================
-- For storing additional user profile information

CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    display_name TEXT,
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Enable RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- RLS Policies
CREATE POLICY "Users can view own profile" 
    ON public.profiles FOR SELECT 
    USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" 
    ON public.profiles FOR UPDATE 
    USING (auth.uid() = id);

-- Function to handle new user signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email)
    VALUES (NEW.id, NEW.email);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to auto-create profile on signup
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

