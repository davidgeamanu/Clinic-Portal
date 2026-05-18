import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Star } from "lucide-react";
import { cn } from "@/lib/utils";

interface RateDoctorDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    doctor: string;
    department?: string;
    submitting?: boolean;
    onSubmit: (rating: number, review: string) => void;
}

export function RateDoctorDialog({ open, onOpenChange, doctor, department, submitting = false, onSubmit }: RateDoctorDialogProps) {
    const [rating, setRating] = useState(0);
    const [hover, setHover] = useState(0);
    const [review, setReview] = useState("");

    const reset = () => { setRating(0); setHover(0); setReview(""); };

    const handleSubmit = () => {
        if (rating < 1 || rating > 5) return;
        onSubmit(rating, review.trim());
    };

    const labels = ["", "Poor", "Fair", "Good", "Very Good", "Excellent"];
    const active = hover || rating;

    return (
        <Dialog open={open} onOpenChange={(o) => { if (!o) reset(); onOpenChange(o); }}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Rate your visit</DialogTitle>
                    <DialogDescription>
                        How was your experience with {doctor}{department ? ` · ${department}` : ""}?
                    </DialogDescription>
                </DialogHeader>
                <div className="space-y-5">
                    <div className="flex flex-col items-center gap-2 py-2">
                        <div className="flex items-center gap-1">
                            {[1, 2, 3, 4, 5].map((s) => (
                                <button
                                    key={s}
                                    type="button"
                                    onClick={() => setRating(s)}
                                    onMouseEnter={() => setHover(s)}
                                    onMouseLeave={() => setHover(0)}
                                    className="p-1 transition-transform hover:scale-110"
                                    aria-label={`${s} star${s > 1 ? "s" : ""}`}
                                >
                                    <Star
                                        className={cn(
                                            "h-9 w-9 transition-colors",
                                            s <= active ? "fill-warning text-warning" : "text-muted-foreground/30"
                                        )}
                                    />
                                </button>
                            ))}
                        </div>
                        <p className="text-sm font-medium text-muted-foreground h-5">{labels[active]}</p>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="review" className="text-sm font-medium">Share your experience (optional)</Label>
                        <Textarea
                            id="review"
                            placeholder="What went well? Anything that could be improved?"
                            value={review}
                            onChange={(e) => setReview(e.target.value)}
                            rows={4}
                            maxLength={500}
                        />
                        <p className="text-xs text-muted-foreground text-right">{review.length}/500</p>
                    </div>

                    <div className="flex gap-2 justify-end">
                        <Button variant="outline" onClick={() => onOpenChange(false)} disabled={submitting}>Cancel</Button>
                        <Button onClick={handleSubmit} disabled={rating === 0 || submitting}>
                            {submitting ? "Submitting..." : "Submit Review"}
                        </Button>
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    );
}
